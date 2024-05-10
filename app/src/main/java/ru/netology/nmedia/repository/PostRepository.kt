package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    suspend fun getAll(): List<Post>
    suspend fun likeById(post: Post): Post
    suspend fun shareById(post: Post): Post
    suspend fun removeById(id: Long): Unit
    suspend fun save(post: Post): Post
}