package ru.netology.nmedia

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.dto.Post


val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likedByMe = false
)

class PostViewModel : ViewModel() {

    private val repository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()
    val edited = MutableLiveData(empty)

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)
    fun edit(post: Post): () -> Unit = { edited.value = post }
    fun clear(): () -> Unit = { edited.value = empty }

    fun changeContentAndSave(content: String) {
        edited.value?.let {
            if (it.content != content) {
                repository.save(it.copy(content = content))
                edited.value = empty
            }
        }
    }
}