package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.NewPostModel
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.utils.SingleLiveEvent
import java.io.IOException
import java.util.Date
import kotlin.concurrent.thread


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

    fun loadPosts(
        getPosts: (() -> List<Post>) = { repository.getAll() },
        addFun: (() -> Unit)? = null
    ) {
        thread {
            _data.postValue(FeedModel(load = true))
            try {
                addFun?.invoke()
                val posts = getPosts.invoke()
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (e: IOException) {
                FeedModel(error = true)
            }.also(_data::postValue)
        }
    }

    fun likeById(post: Post) {
        loadPosts { repository.likeById(post) }
    }

    fun shareById(id: Long) = repository.shareById(id)
    fun getById(id: Long) {
        loadPosts(
            getPosts = { listOf(repository.getById(id)) },
            addFun = { repository.removeById(id) }
        )
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun removeById(id: Long) {
        loadPosts(addFun = { repository.removeById(id) })
    }

    fun clear() {
        edited.value = empty
    }

    fun changeContentAndSave(content: String) {
        edited.value?.let {
            if (it.content != content) {
                thread {
                    _postCreated.postValue(NewPostModel(load = true))
                    try {
                        val newPost = repository
                            .save(it.copy(content = content, published = Date().time))
                        _postCreated.postValue(NewPostModel(post = newPost))
                        edited.postValue(empty)
                        if (newPost.id == 0L) {
                            _postCreated.postValue(NewPostModel(error = true))
                        }
                    } catch (e: IOException) {
                        _postCreated.postValue(NewPostModel(error = true))
                    }
                }
            } else _postCreated.value = NewPostModel(post = it)
        }
    }
}