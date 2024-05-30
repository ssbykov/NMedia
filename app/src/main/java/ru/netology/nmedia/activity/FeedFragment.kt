package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.PostsSetupClickListeners
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.utils.IntArg
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
        var Bundle.urlArg: String? by StringArg
        var Bundle.nextPageArg: Int? by IntArg
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
            binding.emptyTest.isVisible = state.posts.isEmpty()
        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            if (it != null && it > 0) {
                binding.newPosts.text = getString(R.string.new_posts, it.toString())
                binding.newPosts.visibility = View.VISIBLE
            }

        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadPosts()
                    }
                    .setAnchorView(binding.add)
                    .show()
            }
        }

        binding.swiper.setOnRefreshListener {
            currentSize = adapter.currentList.size
            viewModel.loadPosts()
            binding.swiper.isRefreshing = false
            binding.newPosts.visibility = View.GONE
        }

        binding.newPosts.setOnClickListener {
            currentSize = adapter.currentList.size
            viewModel.showAll()
            binding.newPosts.visibility = View.GONE
        }


        binding.add.setOnClickListener {
            viewModel.isLogin.observe(viewLifecycleOwner) { state ->
                if (!state) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_loginFragment,
                        Bundle().apply { nextPageArg = R.id.newPostFragment })
                } else {
                    currentSize = adapter.currentList.size
                    viewModel.dropPhoto()
                    findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
                }
            }
        }

    }
}

