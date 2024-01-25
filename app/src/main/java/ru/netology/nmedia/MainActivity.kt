package ru.netology.nmedia

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.dto.Post

class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = PostsAdapter { postBinding, post ->
            setupClickListeners(postBinding, post)
        }
        binding.root.adapter = adapter
        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
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

}
