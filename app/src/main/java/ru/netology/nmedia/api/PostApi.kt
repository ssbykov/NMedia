package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nmedia.Constants.BASE_URL_SLOW
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

private val retrofit = Retrofit.Builder()
    .client(
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .build()
    )
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL_SLOW)
    .build()

interface PostApiService {
    @GET("posts")
    fun getAll(): Call<List<Post>>
    @DELETE("posts/{id}")
    fun removeById(@Path("id") id: Long): Call<Unit>
    @POST("posts")
    fun save(@Body post: Post): Call<Post>
    @POST("posts/{id}/likes")
    fun likeById(@Path("id") id: Long): Call<Post>
    @DELETE("posts/{id}/likes")
    fun unlikeById(@Path("id") id: Long): Call<Post>
}

object PostApi {
    val retrofitService: PostApiService by lazy {
        retrofit.create<PostApiService>()
    }
}