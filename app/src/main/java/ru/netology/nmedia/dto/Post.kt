package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}

data class Post(
    override val id: Long,
    val author: String,
    val authorId: Long,
    val authorAvatar: String = "netology.jpg",
    val content: String,
    val published: Long,
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val shares: Int = 0,
    val views: Int = 0,
    val video: String? = null,
    val attachment: Attachment? = null,
    val ownedByMy: Boolean = false,
) : FeedItem

data class Ad(
    override val id: Long,
    val image: String,
) : FeedItem


data class Attachment(
    val url: String,
    val type: AttachmentType? = null,
)

enum class AttachmentType {
    IMAGE
}

data class PushToken(
    val token: String,
)