package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryImpl : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    private fun baseRequest(requestBuilder: Request.Builder.() -> Unit): String {
        val builder = Request.Builder()
        builder.requestBuilder()
        val request = builder.build()
        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
    }

    override fun getAll(): List<Post> {
        val response = baseRequest {
            url("$BASE_URL/api/slow/posts")
            get()
        }
        return gson.fromJson(response, typeToken.type)
    }

    override fun removeById(id: Long) {
        baseRequest {
            url("$BASE_URL/api/posts/$id")
            delete(gson.toJson(id).toString().toRequestBody(jsonType))
        }
    }

    override fun save(post: Post): Post {
        val response = baseRequest {
            url("$BASE_URL/api/slow/posts")
            post(gson.toJson(post, Post::class.java).toString().toRequestBody(jsonType))
        }
        return gson.fromJson(response, Post::class.java)
    }

    override fun likeById(post: Post) {
        val body = gson.toJson(post.id).toString().toRequestBody(jsonType)
        baseRequest {
            url("$BASE_URL/api/slow/posts/${post.id}/likes")
            if (post.likedByMe) delete(body) else post(body)
        }
    }


    override fun shareById(id: Long) {
        TODO("Not yet implemented")
    }


    override fun getById(id: Long): Post {
        TODO("Not yet implemented")
    }
}