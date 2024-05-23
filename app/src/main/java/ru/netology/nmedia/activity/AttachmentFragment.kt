package ru.netology.nmedia.activity

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import ru.netology.nmedia.Constants
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.urlArg
import ru.netology.nmedia.adapter.PostsSetupClickListeners.Companion.imageId
import ru.netology.nmedia.databinding.FragmentAttachmentBinding
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class AttachmentFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAttachmentBinding.inflate(inflater, container, false)
        val attachment = arguments?.imageId

        Glide.with(binding.photo)
            .load("${Constants.BASE_URL_IMAGES}$attachment")
            .into(binding.photo)

        return binding.root
    }
}