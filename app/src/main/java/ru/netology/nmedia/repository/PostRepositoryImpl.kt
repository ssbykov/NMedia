package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit

class PostRepositoryImpl : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
//    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999/api/posts"
        private val jsonType = "application/json".toMediaType()
    }

    private fun <T> baseRequest(
        callback: PostRepository.Callback<T>,
        requestBuilder: Request.Builder.() -> Unit
    ) {
        val builder = Request.Builder()
        builder.requestBuilder()
        val request = builder.build()
        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string() ?: throw RuntimeException("body is null")
                    try {
                        val typeToken = object : TypeToken<T>() {}
                        callback.onSuccess(gson.fromJson(body, typeToken))
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            })
    }

    override fun getAll(callback: PostRepository.Callback<List<Post>>) {
        baseRequest(callback) {
            url(BASE_URL)
            get()
        }
    }

    override fun removeById(id: Long, callback: PostRepository.Callback<Post>) {
        baseRequest(callback) {
            url("$BASE_URL/$id")
            delete(gson.toJson(id).toString().toRequestBody(jsonType))
        }
    }

    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        baseRequest(callback) {
            url(BASE_URL)
            post(gson.toJson(post, Post::class.java).toString().toRequestBody(jsonType))
        }
    }

    override fun likeById(post: Post, callback: PostRepository.Callback<Post>) {
        val body = gson.toJson(post.id).toString().toRequestBody(jsonType)
        baseRequest(callback) {
            url("$BASE_URL/${post.id}/likes")
            if (post.likedByMe) delete(body) else post(body)
        }
    }


    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }


    override fun getById(id: Long, callback: PostRepository.Callback<Post>) {
        baseRequest(callback) {
            url("$BASE_URL/$id")
            get()
        }
    }
}