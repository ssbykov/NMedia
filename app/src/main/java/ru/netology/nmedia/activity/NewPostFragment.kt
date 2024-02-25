package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {


    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    companion object {
        var Bundle.textPostID: String? by StringArg
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)
        with(binding) {
            binding.content.setText(arguments?.textArg)
            content.requestFocus()
            AndroidUtils.showKeyboard(content)
            ok.setOnClickListener {
                val postId = viewModel.changeContentAndSave(binding.content.text.toString())
                AndroidUtils.hideKeyboard(requireView())
                findNavController().navigate(
                    R.id.action_newPostFragment_to_feedFragment,
                    Bundle().apply {
                        textPostID = postId
                    }
                )

            }
        }
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        viewModel.clear()
    }
}

