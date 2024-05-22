package ru.netology.nmedia.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
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

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> {
                        val uri = it.data?.data
                        viewModel.changePhoto(uri, uri?.toFile())
                    }
                }
            }

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

            pickPhoto.setOnClickListener {
                ImagePicker.with(this@NewPostFragment)
                    .crop()
                    .compress(2048)
                    .provider(ImageProvider.GALLERY)
                    .galleryMimeTypes(
                        arrayOf(
                            "image/png",
                            "image/jpeg",
                        )
                    )
                    .createIntent(pickPhotoLauncher::launch)

            }

            takePhoto.setOnClickListener {
                ImagePicker.with(this@NewPostFragment)
                    .crop()
                    .compress(2048)
                    .provider(ImageProvider.CAMERA)
                    .createIntent(pickPhotoLauncher::launch)
            }

            removePhoto.setOnClickListener {
                viewModel.dropPhoto()
            }

            viewModel.postCreated.observe(viewLifecycleOwner) { state ->
                if (state != null) {
                    binding.progressNew.isVisible = state.load
                    if (state.error) {
                        Toast.makeText(
                            context,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                if (!state.load) {
                    findNavController().navigateUp()
                }
            }

            viewModel.photo.observe(viewLifecycleOwner) {
                binding.photoContainer.isVisible = it.uri != null
                binding.photo.setImageURI(it.uri)
            }

            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.save -> {
                            viewModel.changeContentAndSave(
                                binding.content.text.toString(),
                                viewModel.photo.value?.uri
                            )
                            AndroidUtils.hideKeyboard(requireView())
                            true
                        }

                        else -> false
                    }
                }

            }, viewLifecycleOwner)

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

