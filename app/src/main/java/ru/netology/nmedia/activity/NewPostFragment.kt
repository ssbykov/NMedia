package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.activity.FeedFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {


    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)
        with(binding) {
            arguments?.textArg?.let ( binding.content::setText )
            val intent = Intent()
//            content.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
            content.requestFocus()
            AndroidUtils.showKeyboard(content)
            ok.setOnClickListener {
                viewModel.changeContentAndSave(binding.content.text.toString())
                AndroidUtils.hideKeyboard(requireView())
                findNavController().navigateUp()
            }
        }
        return binding.root
    }

    override fun onPause() {
        super.onPause()
//        arguments?.textArg = ""
        viewModel.clear()
    }
}

