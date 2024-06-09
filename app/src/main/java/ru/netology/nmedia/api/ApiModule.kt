package ru.netology.nmedia.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.Constants
import ru.netology.nmedia.auth.AppAuth
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Singleton
    @Provides
    fun provideLoggin() = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }


    private fun addAuth(chain: Interceptor.Chain, appAuth: AppAuth): Response {
        val token = appAuth.authStateFlow.value?.token
        with(chain) {
            val request = if (token != null) {
                request().newBuilder().addHeader("Authorization", token).build()
            } else request()
            return proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ) = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .addInterceptor { addAuth(it, appAuth) }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttp: OkHttpClient
    ) = Retrofit.Builder()
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(Constants.BASE_URL_SLOW)
        .build()

    @Provides
    @Singleton
    fun provideApiService(
        retrofit: Retrofit
    ): ApiService = retrofit.create()
}