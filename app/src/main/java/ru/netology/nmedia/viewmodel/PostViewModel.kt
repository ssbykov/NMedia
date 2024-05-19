package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.StateType
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.NewPostModel
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.utils.SingleLiveEvent


val empty = Post(
    id = 0,
    author = "Автор",
    content = "",
    published = 0,
    likedByMe = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PostRepositoryImpl(AppDb.getInstance(application).postDao())
    val data = repository.data.map(::FeedModel)
        .asLiveData(Dispatchers.Default)

    private val postEntites = repository.postEntites.asLiveData(Dispatchers.Default)

    val newerCount = postEntites.switchMap {
        repository.getNewerCoutn(it.filter { postEntite ->
            postEntite.state != StateType.NEW
        }.firstOrNull()?.id ?: 0).asLiveData(Dispatchers.Default)
    }

    private val _postCreated = SingleLiveEvent<NewPostModel>()
    val postCreated: LiveData<NewPostModel>
        get() = _postCreated

    private val _dataState = SingleLiveEvent<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

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
        _dataState.value = FeedModelState()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun showAll() = viewModelScope.launch { repository.showtAll() }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.removeById(id)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun likeById(post: Post) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.likeById(post)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun changeContentAndSave(content: String) {
        edited.value?.let {
            if (it.content != content) {
                _postCreated.value = NewPostModel(load = true)
                viewModelScope.launch {
                    try {
                        _dataState.value = FeedModelState(loading = true)
                        repository.save(
                            it.copy(
                                id = if (it.id == 0L) repository.getLastId() + 1 else it.id,
                                content = content
                            )
                        )
                        edited.value = empty
                    } catch (e: Exception) {
                        _dataState.value = FeedModelState(error = true)
                    } finally {
                        _dataState.value = FeedModelState()
                        _postCreated.value = NewPostModel()
                    }
                }
            }
        }
    }
//
//    fun shareById(post: Post) = viewModelScope.launch {
//        _data.value = _data.value?.copy(load = true)
//        val result = repository.shareById(post)
//        val posts = _data.value?.posts.orEmpty().map {
//            if (it.id == result.id) result else it
//        }
//        _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
//    }
//
//

//

}