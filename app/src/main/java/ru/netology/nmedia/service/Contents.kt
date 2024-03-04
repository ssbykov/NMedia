package ru.netology.nmedia.service

enum class Action(val fields: Array<String>) {
    LIKE(arrayOf("userName", "postAuthor")),
    NEW_POST(arrayOf("userName", "content"))
}

data class Like(
    val userId: Long = 0,
    val userName: String = "",
    val postId: Long = 0,
    val postAuthor: String = "",
)

data class NewPost(
    val userId: Long = 0,
    val userName: String = "",
    val postId: Long = 0,
    val content: String = "",
)