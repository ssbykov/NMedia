package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>
    suspend fun getAll()
    suspend fun showtAll()
    suspend fun likeById(post: Post)
    suspend fun shareById(post: Post)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun getLastId(): Long
    fun getNewerCoutn(id: Long): Flow<Int>
}