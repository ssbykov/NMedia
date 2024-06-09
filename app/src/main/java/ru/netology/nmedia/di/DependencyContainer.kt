package ru.netology.nmedia.di

import android.content.Context
import androidx.room.Room
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.Constants.BASE_URL_SLOW
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.util.concurrent.TimeUnit

class DependencyContainer(
    context: Context
) {

    companion object {
        @Volatile
        private var instance: DependencyContainer? = null

        fun initApp(context: Context) {
            instance = DependencyContainer(context)
        }

        fun getInstance(): DependencyContainer {
            return instance!!
        }

    }

    private val appDb = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .build()

    private val postDao = appDb.postDao()

    private val logging = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    val appAuth = AppAuth(context)
    private fun addAuth(chain: Interceptor.Chain): Response {
        val token = appAuth.authStateFlow.value?.token
        with(chain) {
            val request = if (token != null) {
                request().newBuilder().addHeader("Authorization", token).build()
            } else request()
            return proceed(request)
        }
    }

    private val okhttp = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .addInterceptor { addAuth(it) }
        .build()

    private val retrofit = Retrofit.Builder()
        .client(okhttp)
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL_SLOW)
        .build()

    val apiService = retrofit.create<ApiService>()

    val repository = PostRepositoryImpl(postDao, apiService)

}
