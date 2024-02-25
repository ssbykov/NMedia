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
import ru.netology.nmedia.databinding.FragmentPostBinding
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
        val post = arguments?.textPostID?.let { viewModel.getById(it.toLong()) }
        binding.postCard.content.text = post?.content
        if (post != null) {
            with(binding.postCard) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                like.isChecked = post.likedByMe
                like.text = formatCount(post.likes)
                like.isCheckable = false
                shear.text = formatCount(post.shares)
                views.text = formatCount(post.views)

                if (post.video != null) {
                    play.setOnClickListener {
                        val intent = Intent().apply {
                            action = Intent.ACTION_VIEW
                            data = Uri.parse(post.video)
                        }
                        startActivity(intent, null)
                    }
                    preview.visibility = View.VISIBLE
                    play.visibility = View.VISIBLE
                } else {
                    preview.visibility = View.GONE
                    play.visibility = View.GONE
                }

                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_post)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    viewModel.removeById(post.id)
                                    findNavController().navigateUp()
                                    true
                                }

                                R.id.edit -> {
                                    viewModel.edit(post)
                                    findNavController().navigate(
                                        R.id.action_postFragment_to_newPostFragment,
                                        Bundle().apply {
                                            textArg = post.content
                                        }
                                    )
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }
            }
        }
        return binding.root
    }
}