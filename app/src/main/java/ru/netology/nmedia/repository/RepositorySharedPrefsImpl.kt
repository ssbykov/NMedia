package ru.netology.nmedia.repository

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class PostRepositorySharedPrefsInmpl(
    context: Context
) : PostRepository {
    private val gson = Gson()

    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)

    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type

    private val key = "post"

    private var nextId = 1L

    private var posts = emptyList<Post>()
        private set(value) {
            field = value
            data.value = posts
            sync()
        }

    private val data = MutableLiveData(posts)

    init {
        prefs.getString(key, null)?.let {
            posts = gson.fromJson(it, type)
            nextId = posts.maxOfOrNull { post -> post.id }?.inc() ?: 1
        }
    }

    override fun getAll(): LiveData<List<Post>> = data

    //метод выставления лайка посту
    override fun likeById(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) {
                var likes = if (post.likedByMe) post.likes - 1 else post.likes + 1
                if (likes < 0) likes = 0
                post.copy(likedByMe = !post.likedByMe, likes = likes)
            } else post
        }
    }

    //метод поделиться постом
    override fun shareById(id: Long) {
        posts = posts.map { post ->
            if (post.id == id) post.copy(shares = post.shares + 1) else post
        }
    }

    //метод удаления поста
    override fun removeById(id: Long) {
        posts = posts.filter { post ->
            post.id != id
        }
    }

    //метод сохранения поста
    @SuppressLint("SimpleDateFormat")
    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(
                post.copy(
                    id = nextId++,
                    author = "Автор",
                    likedByMe = false,
                    published = SimpleDateFormat("dd MMMM в H:mm", Locale("ru"))
                        .format(Date())
                )
            ) + posts
        } else {
            posts.map { if (it.id == post.id) post.copy(content = post.content) else it }
        }
    }

    private fun sync() {
        with(prefs.edit()) {
            putString(key, gson.toJson(posts))
            apply()
        }
    }
}