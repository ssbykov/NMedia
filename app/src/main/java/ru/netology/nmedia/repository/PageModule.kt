package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingSourceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.PostEntity
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class PageModule {

    @Provides
    @Singleton
    fun providePagingConfig() = PagingConfig(pageSize = 10, maxSize = 30)

    @Provides
    @Singleton
    fun providePostRemoteMediator(
        apiService: ApiService,
        postDao: PostDao,
        postRemoteKeyDao: PostRemoteKeyDao,
        appDb: AppDb
    ) = PostRemoteMediator(
        apiService = apiService,
        postDao = postDao,
        postRemoteKeyDao = postRemoteKeyDao,
        appDb = appDb
    )


    @OptIn(ExperimentalPagingApi::class)
    @Singleton
    @Provides
    fun providerPager(
        pagingConfig: PagingConfig,
        postDao: PostDao,
        postRemoteMediator: PostRemoteMediator
    ) = Pager(
        config = pagingConfig,
        pagingSourceFactory = { postDao.getPagingSource() },
        remoteMediator = postRemoteMediator
    )
}