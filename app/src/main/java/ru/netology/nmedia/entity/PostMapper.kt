package ru.netology.nmedia.entity

import org.mapstruct.Mapper
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post

@Mapper
interface PostMapper {
    fun fromDto(post: Post): PostEntity

    fun toDto(postEntity: PostEntity): Post

    fun toDtoAttachment(attachment: AttachmentEmbeddable?): Attachment? {
        return attachment?.let { Attachment(it.url, AttachmentType.IMAGE) }
    }

    fun fromDtoAttachment(attachment: Attachment?): AttachmentEmbeddable? {
        return attachment?.let { AttachmentEmbeddable(it.url, AttachmentType.IMAGE) }
    }
}

fun List<PostEntity>.toDto(): List<Post> =
    filter { it.state != StateType.DELETED }.map(PostMapperImpl::toDto)

fun List<Post>.toEntity(): List<PostEntity> = map(PostMapperImpl::fromDto)

object PostMapperImpl : PostMapper {
    override fun fromDto(post: Post) = PostEntity(
        id = post.id,
        author = post.author,
        authorId = post.authorId,
        authorAvatar = post.authorAvatar,
        content = post.content,
        published = post.published,
        likes = post.likes,
        likedByMe = post.likedByMe,
        shares = post.shares,
        views = post.views,
        video = post.video,
        state = null,
        visible = true,
        attachment = fromDtoAttachment(post.attachment)
    )

    override fun toDto(postEntity: PostEntity) = Post(
        postEntity.id,
        postEntity.author,
        postEntity.authorId,
        postEntity.authorAvatar,
        postEntity.content,
        postEntity.published,
        postEntity.likes,
        postEntity.likedByMe,
        postEntity.shares,
        postEntity.views,
        postEntity.video,
        toDtoAttachment(postEntity.attachment)
    )

}