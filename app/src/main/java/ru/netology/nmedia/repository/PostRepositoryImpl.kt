package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post

class PostRepositoryImpl : PostRepository {

    override suspend fun getAll(): List<Post> = PostApi.retrofitService.getAll()

    override suspend fun likeById(post: Post): Post {
        if (post.likedByMe) {
            PostApi.retrofitService.unlikeById(post.id)
        } else {
            PostApi.retrofitService.likeById(post.id)
        }
        return post
    }

    override suspend fun shareById(post: Post): Post =
        PostApi.retrofitService.save(post.copy(shares = post.shares + 1))

    override suspend fun removeById(id: Long) = PostApi.retrofitService.removeById(id)

    override suspend fun save(post: Post): Post = PostApi.retrofitService.save(post)

}