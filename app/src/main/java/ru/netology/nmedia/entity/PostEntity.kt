package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.mapstruct.Mapper
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val video: String? = null
)

@Mapper
interface PostMapper {
    fun fromDto(post: Post): PostEntity
    fun toDto(postEntity: PostEntity): Post
}

class PostMapperImpl : PostMapper {
    override fun fromDto(post: Post) = PostEntity(
        id = post.id,
        author = post.author,
        content = post.content,
        published = post.published,
        likes = post.likes,
        likedByMe = post.likedByMe,
        shares = post.shares,
        views = post.views,
        video = post.video
    )

    override fun toDto(postEntity: PostEntity) = Post(
        postEntity.id,
        postEntity.author,
        postEntity.content,
        postEntity.published,
        postEntity.likes,
        postEntity.likedByMe,
        postEntity.shares,
        postEntity.views,
        postEntity.video
    )

}