package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
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
            val draftPrefs =
                root.context.getSharedPreferences("draft", android.content.Context.MODE_PRIVATE)
            val key = "newPost"

            if (arguments != null) {
                content.setText(arguments?.textArg)
            } else {
                val draft = draftPrefs.getString(key, "").toString()
                content.setText(draft)
                draftPrefs.edit().putString(key, "").apply()
            }

            content.requestFocus()
            AndroidUtils.showKeyboard(content)
            ok.setOnClickListener {
                viewModel.changeContentAndSave(binding.content.text.toString())
                AndroidUtils.hideKeyboard(requireView())
            }

            viewModel.postCreated.observe(viewLifecycleOwner) { state ->
                if (state != null) {
                    binding.progressNew.isVisible = state.load
                    binding.ok.isClickable = !state.load
                    if (state.error) {
                        Toast.makeText(
                            context,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                if (state?.post != null) {
                    findNavController().navigateUp()
                }
            }

            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (arguments != null) {
                        viewModel.clear()
                    } else {
                        draftPrefs.edit().putString(key, content.text.toString()).apply()
                    }
                    findNavController().navigateUp()
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        }
        return binding.root
    }
}

