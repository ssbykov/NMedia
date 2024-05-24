package ru.netology.nmedia.repository

import android.net.Uri
import androidx.core.net.toFile
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
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.AttachmentEmbeddable
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
        synchronize(postEntites)
        try {
            val lastId = postEntites.firstOrNull()?.id ?: 0
            val response = PostApi.retrofitService.getNewer(lastId)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val posts = response.body() ?: throw UnknownError
            dao.insert(
                posts.filter { it.author != "Student" }.toEntity()
                    .map { it.copy(state = null, visible = true) })
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: ApiError) {
            throw e

        } catch (e: Exception) {
            throw UnknownError
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
        synchronize(dao.getAllsync())
    }


    override suspend fun likeById(post: Post) {
        dao.likeById(post.id)
        val newPost = PostMapperImpl.toDto(requireNotNull(dao.getById(post.id)))
        setStateEditedOrNew(newPost)
        synchronize(dao.getAllsync())
    }

    override suspend fun shareById(post: Post) {
        PostApi.retrofitService.save(post.copy(shares = post.shares + 1))
    }


    override suspend fun save(post: Post) {
        setStateEditedOrNew(post)
        synchronize(dao.getAllsync())
    }

    override suspend fun getLastId(): Long {
        return dao.getLastId() ?: 0
    }

    override fun getNewerCoutn(id: Long): Flow<Int> = flow {

        while (true) {
            emit(0)
            delay(10_000L)
            val response = PostApi.retrofitService.getNewer(id)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val newPosts = response.body() ?: throw UnknownError
            dao.insert(
                newPosts.filter { it.author != "Student" }.toEntity()
                    .map { it.copy(state = null, visible = false) })
            emit(dao.getNewerCount())
        }
    }
        .catch { e ->
            throw AppError.from(e)
        }
        .flowOn(Dispatchers.Default)

    private suspend fun setStateEditedOrNew(post: Post) {
        val postEntity = dao.getById(post.id)
        if (postEntity != null && postEntity.state != StateType.NEW) {
            dao.insert(postEntity.copy(
                content = post.content,
                state = StateType.EDITED,
                attachment = PostMapperImpl.fromDtoAttachment(post.attachment)
            ))
        } else {
            dao.insert(
                PostMapperImpl.fromDto(post)
                    .copy(state = StateType.NEW, visible = true)
            )
        }
    }

    private suspend fun setLike(post: Post) {
        try {
            val response = if (post.likedByMe) {
                PostApi.retrofitService.unlikeById(post.id)
            } else {
                PostApi.retrofitService.likeById(post.id)
            }
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: ApiError) {
            throw e

        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: File): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.name, upload.asRequestBody()
            )
            val response = PostApi.retrofitService.upload(media)
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: ApiError) {
            throw e

        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun synchronize(posts: List<PostEntity>?) {
        val postList = posts?.filter { it.state != null && it.visible }
        postList?.forEach { postEntity ->
            try {
                when (postEntity.state) {
                    StateType.NEW -> {
                        val response = PostApi.retrofitService.save(
                            PostMapperImpl.toDto(postEntity).copy(id = 0)
                        )
                        if (!response.isSuccessful) throw ApiError(
                            response.code(),
                            response.message()
                        )
                        dao.removeById(postEntity.id)
                        dao.insert(PostMapperImpl.fromDto(requireNotNull(response.body())))
                        synchronizeLike(response.body(), postEntity)
                    }

                    StateType.EDITED -> {
                        val response =
                            PostApi.retrofitService.save(PostMapperImpl.toDto(postEntity))
                        if (!response.isSuccessful) throw ApiError(
                            response.code(),
                            response.message()
                        )
                        synchronizeLike(response.body(), postEntity)
                    }

                    StateType.DELETED -> {
                        val response = PostApi.retrofitService.removeById(postEntity.id)
                        if (!response.isSuccessful) throw ApiError(
                            response.code(),
                            response.message()
                        )
                        dao.removeById(postEntity.id)
                    }

                    null -> return
                }
            } catch (e: IOException) {
                throw NetworkError

            } catch (e: ApiError) {
                throw e

            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }

    private suspend fun synchronizeLike(post: Post?, postEntity: PostEntity) {
        if (post != null && post.likedByMe != postEntity.likedByMe) {
            setLike(post)
        }
        dao.insert(
            PostMapperImpl.fromDto(
                requireNotNull(post).copy(
                    likes = postEntity.likes,
                    likedByMe = postEntity.likedByMe
                )
            )
        )
    }
}