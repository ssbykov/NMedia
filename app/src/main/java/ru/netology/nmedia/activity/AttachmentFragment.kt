package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import ru.netology.nmedia.Constants
import ru.netology.nmedia.adapter.PostsSetupClickListeners.Companion.imageId
import ru.netology.nmedia.databinding.FragmentAttachmentBinding
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