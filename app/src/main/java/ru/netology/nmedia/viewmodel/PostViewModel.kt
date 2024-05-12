package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.NewPostModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.utils.SingleLiveEvent
import java.util.Date


val empty = Post(
    id = 0,
    author = "Автор",
    content = "",
    published = 0,
    likedByMe = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    private val _postCreated = SingleLiveEvent<NewPostModel>()

    val data: LiveData<FeedModel>
        get() = _data

    val postCreated: LiveData<NewPostModel>
        get() = _postCreated

    val edited = MutableLiveData(empty)

    init {
        loadPosts()
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun clear() {
        edited.value = empty
    }

    fun errorReset() {
        _data.postValue(_data.value?.copy(error = false))
    }

    fun loadPosts() = viewModelScope.launch {
        _data.value = _data.value?.copy(load = true)
        val posts = repository.getAll()
        _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
    }

    fun likeById(post: Post) = viewModelScope.launch {
        _data.value = _data.value?.copy(load = true)
        val result = repository.likeById(post)
        val posts = _data.value?.posts.orEmpty().map {
            if (it.id == result.id) result else it
        }
        _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
    }

    fun shareById(post: Post) = viewModelScope.launch {
        _data.value = _data.value?.copy(load = true)
        val result = repository.shareById(post)
        val posts = _data.value?.posts.orEmpty().map {
            if (it.id == result.id) result else it
        }
        _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
    }


    fun removeById(id: Long) = viewModelScope.launch {
        _data.value = _data.value?.copy(load = true)
        repository.removeById(id)
        val posts = _data.value?.posts.orEmpty().filter { it.id != id }
        _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
    }

    fun changeContentAndSave(content: String) {
        edited.value?.let {
            if (it.content != content) {
                _postCreated.value = NewPostModel(load = true)
                viewModelScope.launch {
                    val result =
                        repository.save(it.copy(content = content, published = Date().time))
                    val posts = _data.value?.posts.orEmpty().filter { post ->
                        post.id != result.id
                    }.plus(result).sortedByDescending { post -> post.id }
                    _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
                    _postCreated.value = NewPostModel()
                    edited.postValue(empty)
                }
            } else _postCreated.value = NewPostModel()
        }
    }
}