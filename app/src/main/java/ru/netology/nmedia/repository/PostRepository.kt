package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(post: Post)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post): Post
    fun getById(id: Long): Post
}