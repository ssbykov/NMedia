package ru.netology.nmedia.adapter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.textArg
import ru.netology.nmedia.activity.FeedFragment.Companion.urlArg
import ru.netology.nmedia.adapter.PostsSetupClickListeners.Companion.imageId
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

interface SetupClickListenersInt {
    fun onLikeListener(post: Post)
    fun onShareListener(post: Post)
    fun onRemoveListener(post: Post)
    fun onEditListener(post: Post)
    fun onPlayListener(post: Post)
    fun onImageListener(imageId: String?)
    fun onPostListener(post: Post) {}
}

open class SetupClickListeners(
    private val viewModel: PostViewModel,
    private val fragment: Fragment,
) : SetupClickListenersInt {

    override fun onLikeListener(post: Post) {
        viewModel.isLogin.observe(fragment.viewLifecycleOwner) { state ->
            if (!state) {
                fragment.findNavController().navigate(R.id.loginFragment)
            } else {
                viewModel.likeById(post)
            }
        }
    }

    override fun onShareListener(post: Post) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, post.content)
        }

        val shareChooser =
            Intent.createChooser(shareIntent, fragment.getString(R.string.chooser_sharing_post))
        fragment.startActivity(shareChooser)
        viewModel.shareById(post)
    }

    override fun onRemoveListener(post: Post) {
        viewModel.removeById(post.id)
        if (viewModel.edited.value?.id == post.id) {
            viewModel.clear()
        }
    }

    override fun onEditListener(post: Post) {
        viewModel.edit(post)
        fragment.findNavController().navigate(
            R.id.newPostFragment,
            Bundle().apply {
                textArg = post.content
                urlArg = post.attachment?.url
            }
        )
    }

    override fun onPlayListener(post: Post) {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(post.video)
        }
        fragment.startActivity(intent, null)
    }

    override fun onImageListener(imageId: String?) {
        fragment.findNavController().navigate(
            R.id.attachmentFragment,
            Bundle().apply {
                this.imageId = imageId
            }
        )
    }
}

class PostSetupClickListeners(
    viewModel: PostViewModel,
    private val fragment: Fragment
) : SetupClickListeners(viewModel, fragment) {
    override fun onRemoveListener(post: Post) {
        super.onRemoveListener(post)
        fragment.findNavController().navigateUp()
    }
}

class PostsSetupClickListeners(
    viewModel: PostViewModel,
    private val fragment: Fragment,
) : SetupClickListeners(viewModel, fragment) {

    companion object {
        var Bundle.textPostID: String? by StringArg
        var Bundle.imageId: String? by StringArg
    }

    override fun onPostListener(post: Post) {
        fragment.findNavController().navigate(
            R.id.action_feedFragment_to_postFragment,
            Bundle().apply {
                textPostID = post.id.toString()
            }
        )
    }
}