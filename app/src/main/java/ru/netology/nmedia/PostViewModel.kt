package ru.netology.nmedia

import androidx.lifecycle.ViewModel

class PostViewModel: ViewModel() {

    private val repository = PostRepositoryInMemoryImpl()
    val data = repository.get()
    fun like() = repository.like()
    fun share() = repository.share()

}