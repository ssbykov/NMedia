package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.textArg
import ru.netology.nmedia.activity.FeedFragment.Companion.textPostID
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.adapter.PostsSetupClickListeners
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.formatCount
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

        viewModel.data.observe(viewLifecycleOwner) { posts ->
            posts.find { it.id == postId }?.let {
                PostViewHolder(binding.postCard, object : PostsSetupClickListeners {
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
                            Intent.createChooser(
                                shareIntent,
                                getString(R.string.chooser_sharing_post)
                            )
                        startActivity(shareChooser)
                        viewModel.shareById(post.id)
                    }

                    override fun onRemoveListener(post: Post) {
                        viewModel.removeById(post.id)
                        if (viewModel.edited.value?.id == post.id) {
                            viewModel.clear()
                        }
                        findNavController().navigateUp()
                    }

                    override fun onEditListener(post: Post) {
                        viewModel.edit(post)
                    }

                    override fun onPlayListener(post: Post) {
                        val intent = Intent().apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(post.video)
                        }
                        startActivity(intent, null)
                    }
                }
                ).bind(it)
            }
        }
        viewModel.edited.observe(viewLifecycleOwner) { post ->
            if (post.id != 0L) {
                findNavController().navigateUp()
            }
        }
        return binding.root
    }
}