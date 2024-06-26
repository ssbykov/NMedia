package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.entity.PostEntity
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun synchronizePosts()
    suspend fun showAll()
    suspend fun likeById(post: Post)
    suspend fun shareById(post: Post)
    suspend fun removeById(id: Long)
    suspend fun getById(id: Long): PostEntity?
    suspend fun save(post: Post)
    suspend fun upload(upload: File): Media?
    suspend fun getLastId(): Long
    suspend fun authentication(login: String, password: String): Token?
    suspend fun registration(login: String, password: String, name: String): Token?
    suspend fun registerWithPhoto(
        login: String,
        password: String,
        name: String,
        upload: File
    ): Token?

    fun getNewerCount(id: Long): Flow<Int>
    suspend fun synchronizeById(id: Long)
}