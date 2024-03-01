package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepositoryRoomImpl
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


val empty = Post(
    id = 0,
    author = "Автор",
    content = "",
    published = "",
    likedByMe = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PostRepositoryRoomImpl(
        AppDb.getInstance(application).PostDao()
    )
    val data = repository.getAll()
    val edited = MutableLiveData(empty)

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)
    fun edit(post: Post) {
        edited.value = post
    }

    fun clear() {
        edited.value = empty
    }

    fun changeContentAndSave(content: String): String? {
        var postId: String? = null
        edited.value?.let {
            val published = SimpleDateFormat("dd MMMM в H:mm", Locale("ru")).format(Date())

            if (it.content != content) {
                repository.save(it.copy(content = content, published = published))
            }
            postId = it.id.toString()
            edited.value = empty
        }
        return postId
    }
}