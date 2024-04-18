package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    fun loadPosts() {
        _data.postValue(_data.value?.copy(load = true))
        repository.getAll(object : PostRepository.PostCallback<List<Post>> {
            override fun onSuccess(result: List<Post>) {
                _data.postValue(FeedModel(posts = result, empty = result.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(load = false, error = true))
            }
        })
    }

    fun likeById(post: Post) {
        _data.postValue(_data.value?.copy(load = true))
        repository.likeById(post, object : PostRepository.PostCallback<Post> {
            override fun onSuccess(result: Post) {
                val posts = _data.value?.posts.orEmpty().map {
                    if (it.id == result.id) result else it
                }
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(load = false, error = true))
            }
        })
    }

    fun shareById(post: Post) {
        _data.postValue(_data.value?.copy(load = true))
        repository.shareById(post, object : PostRepository.PostCallback<Post> {
            override fun onSuccess(result: Post) {
                val posts = _data.value?.posts.orEmpty().map {
                    if (it.id == result.id) result else it
                }
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(_data.value?.copy(load = false, error = true))
            }
        })
    }


    fun removeById(id: Long) {
        _data.postValue(_data.value?.copy(load = true))
        repository.removeById(id, object : PostRepository.PostCallback<Unit> {
            override fun onSuccess(result: Unit) {
                val posts = _data.value?.posts.orEmpty().filter { it.id != id }
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun changeContentAndSave(content: String) {
        edited.value?.let {
            if (it.content != content) {
                _postCreated.postValue(NewPostModel(load = true))
                repository.save(it.copy(content = content, published = Date().time),
                    object : PostRepository.PostCallback<Post> {
                        override fun onSuccess(result: Post) {
                            val posts = _data.value?.posts.orEmpty().filter { post ->
                                post.id != result.id
                            }.plus(result).sortedByDescending { post -> post.id }
                            _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
                            _postCreated.postValue(NewPostModel(post = result))
                            edited.postValue(empty)
                        }

                        override fun onError(e: Exception) {
                            _postCreated.postValue(NewPostModel(error = true))
                        }
                    })
            }
        }
    }
}
