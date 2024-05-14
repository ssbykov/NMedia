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
    val authorAvatar: String = "netology.jpg",
    val content: String,
    val published: Long,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val video: String? = null,
    val state: StateType? = StateType.NEW
)

enum class StateType {
    NEW, EDITED, DELETED
}

@Mapper
interface PostMapper {
    fun fromDto(post: Post): PostEntity
    fun toDto(postEntity: PostEntity): Post
}

fun List<PostEntity>.toDto(): List<Post> = map(PostMapperImpl::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostMapperImpl::fromDto)

object PostMapperImpl : PostMapper {
    override fun fromDto(post: Post) = PostEntity(
        id = post.id,
        author = post.author,
        authorAvatar = post.authorAvatar,
        content = post.content,
        published = post.published,
        likes = post.likes,
        likedByMe = post.likedByMe,
        shares = post.shares,
        views = post.views,
        video = post.video,
        state = null
    )

    override fun toDto(postEntity: PostEntity) = Post(
        postEntity.id,
        postEntity.author,
        postEntity.authorAvatar,
        postEntity.content,
        postEntity.published,
        postEntity.likes,
        postEntity.likedByMe,
        postEntity.shares,
        postEntity.views,
        postEntity.video
    )

}