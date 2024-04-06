package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val postId: Long = (arguments?.textPostID ?: return binding.root).toLong()

//        viewModel.data.value?.let { feedModel ->
//            feedModel.posts.find { it.id == postId }
//        }?.let { post ->
//            PostViewHolder(
//                binding.postCard,
//                PostSetupClickListeners(viewModel, this)
//            ).bind(post)
//        }
//
        viewModel.data.observe(viewLifecycleOwner) { feedModel ->
            val post = feedModel?.posts?.find { it.id == postId }
            if (post != null) {
                PostViewHolder(
                    binding.postCard,
                    PostSetupClickListeners(viewModel, this)
                ).bind(post)
            }
        }
        return binding.root
    }
}