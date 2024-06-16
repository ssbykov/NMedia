package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {
        try {
            val pageSize = state.config.pageSize
            val response = when (loadType) {
                LoadType.REFRESH -> {
                    apiService.getLatest(pageSize)
                }

                LoadType.PREPEND -> {
                    val id = postRemoteKeyDao.max() ?: return MediatorResult.Success(false)
                    apiService.getAfter(id, pageSize)
                }

                LoadType.APPEND -> {
                    val id = postRemoteKeyDao.min() ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body().orEmpty()

            appDb.withTransaction {
                when (loadType) {
                    LoadType.REFRESH -> {
                        postDao.clear()
                        postRemoteKeyDao.insert(
                            listOf(
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.AFTER,
                                    body.first().id
                                ),
                                PostRemoteKeyEntity(
                                    PostRemoteKeyEntity.KeyType.BEFORE,
                                    body.last().id
                                )
                            )
                        )
                    }

                    LoadType.PREPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.AFTER,
                                body.first().id
                            )
                        )

                    }

                    LoadType.APPEND -> {
                        postRemoteKeyDao.insert(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.last().id
                            )
                        )
                    }
                }

                postDao.insert(body.toEntity())
            }
            return MediatorResult.Success(body.isEmpty())
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}