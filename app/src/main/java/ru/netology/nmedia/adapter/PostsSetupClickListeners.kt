package ru.netology.nmedia.adapter

import ru.netology.nmedia.dto.Post

interface PostsSetupClickListeners {
    fun onLikeListener(post: Post)
    fun onShareListener(post: Post)
    fun onRemoveListener(post: Post)
    fun onEditListener(post: Post)
    fun onPlayListener(post: Post)
    fun onPostListener(post: Post) {}
}