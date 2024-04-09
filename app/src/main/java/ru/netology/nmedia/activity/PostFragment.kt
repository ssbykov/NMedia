package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import ru.netology.nmedia.adapter.PostSetupClickListeners
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.adapter.PostsSetupClickListeners.Companion.textPostID
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel

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

        viewModel.data.observe(viewLifecycleOwner) { state ->
            if (state.changed) {
                viewModel.loadPosts()
                return@observe
            }
            val post = state?.posts?.find { it.id == postId }
            if (post != null && !state.load) {
                PostViewHolder(
                    binding.postCard,
                    PostSetupClickListeners(viewModel, this)
                ).bind(post)
            }

            binding.progressPost.isVisible = state.load
            binding.postCard.root.isVisible = !state.load

        }
        return binding.root
    }
}