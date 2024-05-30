package ru.netology.nmedia.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostMapperImpl
import ru.netology.nmedia.entity.StateType
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    val postEntites = dao.getAll()
    override val data = dao.getAllVisible()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        val postEntites = dao.getAllsync()
        val localSynchronizedPosts = postEntites.filter { it.state != StateType.NEW }
        synchronize(postEntites, dao)
        try {
            val response =
                PostApi.retrofitService.getNewer(localSynchronizedPosts.firstOrNull()?.id ?: 0)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val newPosts = response.body() ?: throw UnknownError
            insertNewApiPosts(newPosts)
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: ApiError) {
            throw e

        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getNewerCoutn(id: Long): Flow<Int> = flow {

        while (true) {
            emit(0)
            delay(10_000L)
            val response = PostApi.retrofitService.getNewer(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val newPosts = response.body() ?: throw UnknownError
            insertNewApiPosts(newPosts)

            emit(dao.getNewerCount())
        }
    }
        .catch { e ->
            throw AppError.from(e)
        }
        .flowOn(Dispatchers.Default)

    private suspend fun insertNewApiPosts(newApiPosts: List<Post>) {
        val newLocalPosts = dao.getAllsync().filter { it.state == StateType.NEW }
        val authorId = AppAuth.getInstance().authStateFlow.value?.id
        if (newLocalPosts.size == 0) {
            dao.insert(
                newApiPosts.toEntity()
                    .map {
                        it.copy(
                            state = null,
                            visible = if (it.authorId == authorId) true else false
                        )
                    })
        }

    }

    override suspend fun showtAll() {
        dao.showAll()
    }

    override suspend fun removeById(id: Long) {
        val postEntity = dao.getById(id)
        if (postEntity?.state == StateType.NEW) {
            dao.removeById(id)
        } else {
            postEntity?.copy(state = StateType.DELETED)?.let { dao.insert(it) }
        }
        synchronize(dao.getAllsync(), dao)
    }


    override suspend fun likeById(post: Post) {
        val postEntity = dao.getById(post.id)
        if (postEntity?.state == null) {
            try {
                dao.likeById(post.id)
                val response = if (post.likedByMe) {
                    PostApi.retrofitService.unlikeById(post.id)
                } else {
                    PostApi.retrofitService.likeById(post.id)
                }
                if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            } catch (e: Exception) {
                dao.likeById(post.id)
                throw e
            }
        }
    }

    override suspend fun shareById(post: Post) {
        PostApi.retrofitService.save(post.copy(shares = post.shares + 1))
    }


    override suspend fun save(post: Post) {
        setStateEditedOrNew(post)
        synchronize(dao.getAllsync(), dao)
    }

    override suspend fun getLastId(): Long {
        return dao.getLastId() ?: 0
    }

    override suspend fun login(login: String, password: String): Token? {
        try {
            val response = PostApi.retrofitService.login(login, password)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            return response.body()
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: ApiError) {
            throw e

        } catch (e: Exception) {
            throw UnknownError
        }
    }


    private suspend fun setStateEditedOrNew(post: Post) {
        val postEntity = dao.getById(post.id)
        if (postEntity != null && postEntity.state != StateType.NEW) {
            dao.insert(
                PostMapperImpl.fromDto(post).copy(state = StateType.EDITED)
            )
        } else {
            dao.insert(
                PostMapperImpl.fromDto(post).copy(state = StateType.NEW, visible = true)
            )
        }
    }

    override suspend fun upload(upload: File): Media? {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.name, upload.asRequestBody()
            )
            val response = PostApi.retrofitService.upload(media)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            return null

        } catch (e: ApiError) {
            throw e

        } catch (e: Exception) {
            throw UnknownError
        }
    }

}