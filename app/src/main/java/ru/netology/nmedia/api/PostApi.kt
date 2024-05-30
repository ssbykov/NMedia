package ru.netology.nmedia.api

import okhttp3.Interceptor.Chain
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Response as response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.Constants.BASE_URL_SLOW
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Token
import java.util.concurrent.TimeUnit


private val logging = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG) {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

private fun addAuth(chain: Chain): response {
    val token = AppAuth.getInstance().authStateFlow.value?.token
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

interface PostApiService {
    @GET("posts")
    suspend fun getAll(): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @DELETE("posts/{id}")
    suspend fun removeById(@Path("id") id: Long): Response<Unit>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun authentication(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<Token>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registration(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String,
    ): Response<Token>

    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun unlikeById(@Path("id") id: Long): Response<Post>
}

object PostApi {
    val retrofitService: PostApiService by lazy {
        retrofit.create<PostApiService>()
    }
}