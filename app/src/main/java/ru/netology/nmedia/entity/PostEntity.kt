package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.AttachmentType

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
    val state: StateType? = StateType.NEW,
    val visible: Boolean = true,
    @Embedded
    val attachment: AttachmentEmbeddable?
)

data class AttachmentEmbeddable(
    val url: String,
    val type: AttachmentType,
)

enum class StateType {
    NEW, EDITED, DELETED
}

