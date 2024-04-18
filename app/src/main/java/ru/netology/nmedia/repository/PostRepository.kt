package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(callback: PostCallback<List<Post>>)
    fun likeById(post: Post, callback: PostCallback<Post>)
    fun shareById(post: Post, callback: PostCallback<Post>)
    fun removeById(id: Long, callback: PostCallback<Unit>)
    fun save(post: Post, callback: PostCallback<Post>)

    interface PostCallback<T> {
        fun onSuccess(result: T) {}
        fun onError(e: Exception) {}
    }
}