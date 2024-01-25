package ru.netology.nmedia

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.formatCount

class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.data.observe(this) { posts ->
            binding.container.removeAllViews()
            posts.forEach { post ->
                PostCardBinding.inflate(layoutInflater, binding.container, true).apply {
                    setViews(this, post)
                    setupClickListeners(this, post)
                }.root
            }
        }
    }

    private fun setupClickListeners(binding: PostCardBinding, post: Post) {
        with(binding) {
            like.setOnClickListener {
                viewModel.likeById(post.id)
            }
            shear.setOnClickListener {
                viewModel.shareById(post.id)
            }
        }
    }

    private fun setViews(binding: PostCardBinding, post: Post) {
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likeCount.text = formatCount(post.likes)
            shareCount.text = formatCount(post.shares)
            viewsCount.text = formatCount(post.views)
            if (post.likedByMe) {
                like.setImageResource(R.drawable.ic_liked_24)
            } else like.setImageResource(R.drawable.ic_like_24)
        }
    }
}
