package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Token
import java.io.File

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun showtAll()
    suspend fun likeById(post: Post)
    suspend fun shareById(post: Post)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun  upload(upload: File): Media?
    suspend fun getLastId(): Long
    suspend fun login(login: String, password: String): Token?
    fun getNewerCoutn(id: Long): Flow<Int>
}