package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

class NewPostModel (
    val post: Post? = null,
    val load: Boolean = false,
    val error: Boolean = false,
)