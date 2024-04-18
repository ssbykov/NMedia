package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post

class PostRepositoryImpl : PostRepository {


    private fun <T> baseRequest(
        callback: PostRepository.PostCallback<T>,
        call: () -> Call<T>,
    ) {

        call.invoke().enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                println("Код ответа ${response.code()}")
                println("Код ответа ${response.errorBody()}")
                if (response.code() !in (200..299)) {
                    callback.onError(RuntimeException(response.errorBody()?.string()))
                    return
                }
                val body = response.body() ?: throw RuntimeException("body is null")
                callback.onSuccess(body)
            }

            override fun onFailure(p0: Call<T>, t: Throwable) {
                callback.onError(Exception(t))
            }

        })
    }

    override fun getAll(callback: PostRepository.PostCallback<List<Post>>) {
        baseRequest(callback) {
            PostApi.retrofitService.getAll()
        }
    }

    override fun likeById(post: Post, callback: PostRepository.PostCallback<Post>) {
        baseRequest(callback) {
            if (post.likedByMe) {
                PostApi.retrofitService.unlikeById(post.id)
            } else {
                PostApi.retrofitService.likeById(post.id)
            }
        }
    }

    override fun removeById(id: Long, callback: PostRepository.PostCallback<Unit>) {
        baseRequest(callback) {
            PostApi.retrofitService.removeById(id)
        }
    }

    override fun save(post: Post, callback: PostRepository.PostCallback<Post>) {
        baseRequest(callback) {
            PostApi.retrofitService.save(post)
        }
    }


    override fun shareById(post: Post, callback: PostRepository.PostCallback<Post>) {
        baseRequest(callback) {
            PostApi.retrofitService.save(post.copy(shares = post.shares + 1))
        }
    }

}