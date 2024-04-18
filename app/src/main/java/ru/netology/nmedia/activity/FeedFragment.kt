package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.PostsSetupClickListeners
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    private lateinit var binding: FragmentFeedBinding
    private var currentSize = 0

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PostsAdapter(PostsSetupClickListeners(viewModel, this))
        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { state ->
            val newSize = state?.posts?.size ?: 0

            adapter.submitList(state.posts) {
                if (currentSize in 1..<newSize) {
                    binding.list.smoothScrollToPosition(0)
                    currentSize = newSize
                }
            }
            binding.errorGroup.isVisible = state.error && state.posts.isEmpty() && !state.load
            binding.progress.isVisible = state.load
            binding.emptyTest.isVisible = state.posts.isEmpty() && !state.load
            if (state.error && state.posts.isNotEmpty()) {
                Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
                viewModel.errorReset()
            }
        }

        binding.retry.setOnClickListener {
            binding.errorGroup.isVisible = false
            viewModel.loadPosts()
        }

        binding.swiper.setOnRefreshListener {
            viewModel.loadPosts()
            binding.swiper.isRefreshing = false
        }

        binding.add.setOnClickListener {
            currentSize = adapter.currentList.size
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

    }
}

