package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(callback: Callback<List<Post>>)
    fun likeById(post: Post, callback: Callback<Post>)
    fun shareById(id: Long)
    fun removeById(id: Long, callback: Callback<Post>)
    fun save(post: Post, callback: Callback<Post>)
    fun getById(id: Long, callback: Callback<Post>)

    interface Callback<T> {
        fun onSuccess(result: T) {}
        fun onError(e: Exception) {}
    }
}