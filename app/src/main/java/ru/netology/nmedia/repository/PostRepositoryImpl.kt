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
        synchronize(dao.getAllsync())
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

    private suspend fun setStateEditedOrNew(post: Post) {
        val postEntity = dao.getById(post.id)
        if (postEntity != null && postEntity.state != StateType.NEW) {
            dao.insert(postEntity.copy(content = post.content, state = StateType.EDITED))
        } else {
            dao.insert(
                PostMapperImpl.fromDto(post)
                    .copy(state = StateType.NEW)
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

    private suspend fun synchronize(posts: List<PostEntity>? = postEntites.value) {
        posts?.filter { it.state != null }?.forEach { postEntity ->
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