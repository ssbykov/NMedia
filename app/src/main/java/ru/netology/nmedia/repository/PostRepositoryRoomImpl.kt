package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostMapperImpl

class PostRepositoryRoomImpl(
    private val dao: PostDao
) : PostRepository {

    private val postMapper = PostMapperImpl()

    override fun getAll(): LiveData<List<Post>> = dao.getAll().map { list ->
        list.map { postMapper.toDto(it) }
    }

    override fun likeById(id: Long) = dao.likeById(id)

    override fun shareById(id: Long) = dao.shareById(id)

    override fun removeById(id: Long) = dao.removeById(id)

    override fun save(post: Post) = dao.save(postMapper.fromDto(post))

    override fun getById(id: Long): Post = postMapper.toDto(dao.getById(id))

}
