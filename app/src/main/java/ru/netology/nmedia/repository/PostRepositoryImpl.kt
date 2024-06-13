package ru.netology.nmedia.repository

import androidx.lifecycle.asLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.entity.PostMapperImpl
import ru.netology.nmedia.entity.StateType
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: ApiService
) : PostRepository {

    @Inject
    lateinit var appAuth: AppAuth

    val postEntites = dao.getAll()
    override val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false, maxSize = 30),
        pagingSourceFactory = { dao.getAllSource() }
    ).flow.map { pagingData ->
        pagingData
            .map { postEntity ->
                PostMapperImpl.toDto(postEntity)
            }
    }

    override suspend fun getAll() {
        val postEntites = dao.getAllSync()
        val localSynchronizedPosts = postEntites.filter { it.state != StateType.NEW }
        synchronize(postEntites, dao, apiService)
        try {
            val response =
                apiService.getNewer(localSynchronizedPosts.firstOrNull()?.id ?: 0)
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
            val response = apiService.getNewer(id)
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
        val newLocalPosts = dao.getAllSync().filter { it.state == StateType.NEW }
        val authorId = appAuth.authStateFlow.value?.id
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

    override suspend fun showAll() {
        dao.showAll()
    }

    override suspend fun removeById(id: Long) {
        val postEntity = dao.getById(id)
        if (postEntity?.state == StateType.NEW) {
            dao.removeById(id)
        } else {
            postEntity?.copy(state = StateType.DELETED)?.let { dao.insert(it) }
        }
        synchronize(dao.getAllSync(), dao, apiService)
    }


    override suspend fun likeById(post: Post) {
        val postEntity = dao.getById(post.id)
        if (postEntity?.state == null) {
            try {
                dao.likeById(post.id)
                val response = if (post.likedByMe) {
                    apiService.unlikeById(post.id)
                } else {
                    apiService.likeById(post.id)
                }
                if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            } catch (e: Exception) {
                dao.likeById(post.id)
                throw e
            }
        }
    }

    override suspend fun shareById(post: Post) {
        apiService.save(post.copy(shares = post.shares + 1))
    }


    override suspend fun save(post: Post) {
        setStateEditedOrNew(post)
        synchronize(dao.getAllSync(), dao, apiService)
    }

    override suspend fun getLastId(): Long {
        return dao.getLastId() ?: 0
    }

    override suspend fun authentication(login: String, password: String): Token? {
        try {
            val response = apiService.authentication(login, password)
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

    override suspend fun registration(login: String, password: String, name: String): Token? {
        try {
            val response = apiService.registration(login, password, name)
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

    override suspend fun registerWithPhoto(
        login: String,
        password: String,
        name: String,
        upload: File
    ): Token? {
        try {
            val loginPart = login.toRequestBody("text/plain".toMediaType())
            val passwordPart = password.toRequestBody("text/plain".toMediaType())
            val namePart = name.toRequestBody("text/plain".toMediaType())
            val media = MultipartBody.Part.createFormData(
                "file", upload.name, upload.asRequestBody()
            )
            val response =
                apiService.registerWithPhoto(loginPart, passwordPart, namePart, media)
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
            val response = apiService.upload(media)
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