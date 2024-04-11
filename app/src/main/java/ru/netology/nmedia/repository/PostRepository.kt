package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(callback: PostCallback<List<Post>>)
    fun likeById(post: Post, callback: PostCallback<Post>)
    fun shareById(id: Long)
    fun removeById(id: Long, callback: PostCallback<Post>)
    fun save(post: Post, callback: PostCallback<Post>)
    fun getById(id: Long, callback: PostCallback<Post>)

    interface PostCallback<T> {
        fun onSuccess(result: T) {}
        fun onSuccess() {}
        fun onError(e: Exception) {}
    }
}