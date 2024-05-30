package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

class LoginState (
    val logining: Boolean = false,
    val error: Boolean = false,
)