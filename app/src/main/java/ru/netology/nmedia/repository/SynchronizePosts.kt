package ru.netology.nmedia.repository

import ru.netology.nmedia.api.Api
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostMapperImpl
import ru.netology.nmedia.entity.StateType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException


suspend fun synchronize(posts: List<PostEntity>?, dao: PostDao) {
    val postList = posts?.filter { it.state != null && it.visible }
    postList?.forEach { postEntity ->
        try {
            val newPost = when (postEntity.state) {
                StateType.NEW -> {
                    val response = Api.retrofitService.save(
                        PostMapperImpl.toDto(postEntity).copy(id = 0)
                    )
                    if (!response.isSuccessful) throw ApiError(
                        response.code(),
                        response.message()
                    )
                    val post = response.body()
                    dao.removeById(postEntity.id)
                    dao.insert(PostMapperImpl.fromDto(requireNotNull(post)))
                    post
                }

                StateType.EDITED -> {
                    val response =
                        Api.retrofitService.save(PostMapperImpl.toDto(postEntity))
                    if (!response.isSuccessful) throw ApiError(
                        response.code(),
                        response.message()
                    )
                    response.body()
                }

                StateType.DELETED -> {
                    val response = Api.retrofitService.removeById(postEntity.id)
                    if (!response.isSuccessful) throw ApiError(
                        response.code(),
                        response.message()
                    )
                    dao.removeById(postEntity.id)
                    null
                }

                else -> null
            }
            if (newPost != null) {
                dao.insert(PostMapperImpl.fromDto(newPost))
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

