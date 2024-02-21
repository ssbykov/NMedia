package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.SetupClickListeners
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newPostLauncher = registerForActivityResult(NewPostContract) { result ->
            if (result.isNullOrBlank()) {
                Toast.makeText(this, R.string.error_empty_content, Toast.LENGTH_LONG).show()
            } else viewModel.changeContentAndSave(result)
        }

        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
        }

        val adapter = PostsAdapter(object : SetupClickListeners {
            override fun onLikeListener(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShareListener(post: Post) {
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }

                val shareChooser =
                    Intent.createChooser(shareIntent, getString(R.string.chooser_sharing_post))
                startActivity(shareChooser)
                viewModel.shareById(post.id)
            }

            override fun onRemoveListener(post: Post) {
                viewModel.removeById(post.id)
                if (viewModel.edited.value?.id == post.id) {
                    viewModel.clear()
                }
            }

            override fun onEditListener(post: Post) {
                viewModel.edit(post)
            }
        }, intent)
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            val newPost = adapter.currentList.size < posts.size && adapter.currentList.size > 0
            adapter.submitList(posts) {
                if (newPost) binding.list.smoothScrollToPosition(0)
            }
        }
        viewModel.edited.observe(this) { post ->
            if (post.id != 0L) {
                newPostLauncher.launch(post.content)
            }
        }
        binding.add.setOnClickListener {
            newPostLauncher.launch(null)
        }
    }
}

