package ru.netology.nmedia.activity

import android.app.Activity
import android.net.Uri
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
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.Constants
import ru.netology.nmedia.Constants.KEY_ATTACHMENT
import ru.netology.nmedia.Constants.KEY_CONTENT
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.FeedFragment.Companion.textArg
import ru.netology.nmedia.activity.FeedFragment.Companion.urlArg
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.model.PhotoModel
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

            if (arguments?.textArg != null) {
                content.setText(arguments?.textArg)
                if (arguments?.urlArg != null) {
                    Glide.with(binding.photo)
                        .load("${Constants.BASE_URL_IMAGES}${arguments?.urlArg}")
                        .into(binding.photo)
                    val uri = Uri.parse(arguments?.urlArg)
                    viewModel.changePhoto(uri)
                } else viewModel.dropPhoto()
            } else {
                val contint = draftPrefs.getString(KEY_CONTENT, "").toString()
                content.setText(contint)
                val uri = Uri.parse(draftPrefs.getString(KEY_ATTACHMENT, "").toString())
                if (uri.toString() != "null" && uri.toString() != "") viewModel.changePhoto(
                    uri,
                    uri.toFile()
                )
                draftPrefs.edit().putString(KEY_CONTENT, "").apply()
                draftPrefs.edit().putString(KEY_ATTACHMENT, "").apply()
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
                if (it.file != null) {
                    binding.photo.setImageURI(it.uri)
                }
            }

            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_new_post, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.save -> {
                            val attachment = if (viewModel.photo.value != PhotoModel()) {
                                Attachment(viewModel.photo.value?.uri.toString())
                            } else null
                            viewModel.changeContentAndSave(
                                binding.content.text.toString(),
                                attachment
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
                        viewModel.dropPhoto()
                    } else {
                        draftPrefs.edit().putString(KEY_CONTENT, content.text.toString())
                            .apply()
                        val uri = viewModel.photo.value?.uri.toString()
                        draftPrefs.edit().putString(KEY_ATTACHMENT, uri).apply()
                    }
                    findNavController().navigateUp()
                }
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        }
        return binding.root
    }
}

