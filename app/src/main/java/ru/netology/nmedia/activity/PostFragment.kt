package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostSetupClickListeners
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.adapter.PostsSetupClickListeners.Companion.textPostID
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class PostFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentPostBinding.inflate(inflater, container, false)
        val postId = (arguments?.textPostID ?: return binding.root).toLong()

        viewModel.getById(postId)

        viewModel.postById.observe(viewLifecycleOwner) { post ->
            if (post != null) {
                PostViewHolder(
                    binding.postCard,
                    PostSetupClickListeners(viewModel, this)
                ).bind(post)
            }
        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progressPost.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry_loading) { viewModel.synchronizePosts() }
                    .show()
            }
        }
        return binding.root
    }
}