package ru.netology.nmedia.repository

import androidx.lifecycle.map
import okio.IOException
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostMapperImpl
import ru.netology.nmedia.entity.StateType
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {

    private val postEntites = dao.getAll()
    override val data = postEntites.map(List<PostEntity>::toDto)

    override suspend fun getAll() {
        try {
            val response = PostApi.retrofitService.getAll()
            if (!response.isSuccessful) throw ApiError(response.code(), response.message())
            val posts = response.body() ?: throw UnknownError
            dao.insert(posts.toEntity().map { it.copy(state = null) })
        } catch (e: IOException) {
            throw NetworkError

        } catch (e: ApiError) {
            throw e

        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        val postEntity = dao.getById(id)
        if (postEntity.state == StateType.NEW) {
            dao.removeById(id)
        } else {
            dao.insert(postEntity.copy(state = StateType.DELETED))
        }
        synchronize()
    }


    override suspend fun likeById(post: Post) {
        try {
            dao.likeById(post.id)
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

    override suspend fun shareById(post: Post) {
        PostApi.retrofitService.save(post.copy(shares = post.shares + 1))
    }


    override suspend fun save(post: Post) {
        val postEntity = dao.getById(post.id)
        if (postEntity != null && postEntity.state != StateType.NEW) {
            dao.insert(postEntity.copy(content = post.content, state = StateType.EDITED))
        } else {
            dao.insert(
                PostMapperImpl.fromDto(post)
                    .copy(state = StateType.NEW)
            )
        }
        synchronize()
    }

    override suspend fun getLastId(): Long {
        return dao.getLastId()
    }

    suspend fun synchronize() {
        postEntites.value?.filter { it.state != null }?.forEach { postEntity ->
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
                    }

                    StateType.EDITED -> {
                        val response =
                            PostApi.retrofitService.save(PostMapperImpl.toDto(postEntity))
                        if (!response.isSuccessful) throw ApiError(
                            response.code(),
                            response.message()
                        )
                        dao.insert(PostMapperImpl.fromDto(requireNotNull(response.body())))
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

}