package ru.netology.nmedia.service


open class NewNotification

enum class Action(val fields: Array<String>) {
    LIKE(arrayOf("userName", "postAuthor")),
    NEW_POST(arrayOf("userName", "content"))
}

data class Like(
    val userId: Long = 0,
    val userName: String = "",
    val postId: Long = 0,
    val postAuthor: String = "",
): NewNotification()

data class NewPost(
    val userId: Long = 0,
    val userName: String = "",
    val postId: Long = 0,
    val content: String = "",
): NewNotification()

data class NewMailing(
    val recipientId: Long? = 0,
    val content: String = "",
): NewNotification()