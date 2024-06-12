package ru.netology.nmedia.activity

import com.google.android.gms.common.GoogleApiAvailability
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class ActivityModule {

    @Singleton
    @Provides
    fun provideGoogleApi() = GoogleApiAvailability.getInstance()
}