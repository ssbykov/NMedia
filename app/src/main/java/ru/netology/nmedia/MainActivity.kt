package ru.netology.nmedia

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.SetupClickListeners
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.AndroidUtils

class MainActivity : AppCompatActivity() {

    private val viewModel: PostViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = PostsAdapter(object : SetupClickListeners {
            override fun onLikeListener(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShareListener(post: Post) {
                viewModel.shareById(post.id)
            }

            override fun onRemoveListener(post: Post) {
                viewModel.removeById(post.id)
                if (viewModel.edited.value?.id == post.id) {
                    viewModel.clear()
                    clearEdit(binding)
                }
            }

            override fun onEditListener(post: Post) {
                viewModel.edit(post)
                binding.shortContent.text = post.content
                binding.group.visibility = View.VISIBLE
            }
        })
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            val newPost = adapter.currentList.size < posts.size && adapter.currentList.size > 0
            adapter.submitList(posts) {
                if (newPost) binding.list.smoothScrollToPosition(0)
            }
        }
        viewModel.edited.observe(this) { post ->
            if (post.id != 0L) {
                with(binding.content) {
                    requestFocus()
                    setText(post.content)
                }
                AndroidUtils.showKeyboard(binding.content)
            }
        }
        binding.save.setOnClickListener {
            val text = binding.content.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_content, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.changeContentAndSave(text)
            clearEdit(binding)
        }

        binding.close.setOnClickListener {
            viewModel.clear()
            clearEdit(binding)
        }

    }

    private fun clearEdit(binding: ActivityMainBinding) {
        with(binding) {
            group.visibility = View.GONE
            shortContent.text = ""
            content.setText("")
            content.clearFocus()
            AndroidUtils.hideKeyboard(content)
        }
    }
}

