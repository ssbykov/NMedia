package ru.netology.nmedia.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.viewmodel.RegistrationViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory

class RegistrationFragment : Fragment() {

    private val dependencyContainer = DependencyContainer.getInstance()
    private val viewModel: RegistrationViewModel by viewModels(
        factoryProducer = {
            ViewModelFactory(dependencyContainer.repository, dependencyContainer.appAuth)
        }
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        with(binding) {
            registrationButton.setOnClickListener {
                val login = loginRegistrationEditText.text.toString()
                val name = nameRegistrationEditText.text.toString()
                val password = passwordRegistrationEditText.text.toString()
                val confirmPassword = passwordRegistrationEditText2.text.toString()

                if (name.isNullOrEmpty()) {
                    nameRegistrationLayout.error = getString(R.string.required)
                    return@setOnClickListener
                } else nameRegistrationLayout.error = null

                if (login.isNullOrEmpty()) {
                    loginRegistrationLayout.error = getString(R.string.required)
                    return@setOnClickListener
                } else loginRegistrationLayout.error = null

                if (password.isNullOrEmpty()) {
                    passwordRegistrationLayout.error = getString(R.string.required)
                    return@setOnClickListener
                } else passwordRegistrationLayout.error = null

                if (password != confirmPassword) {
                    passwordRegistrationLayout2.error = "Пароли не совпадают"
                    return@setOnClickListener
                } else passwordRegistrationLayout2.error = null

                viewModel.avatar.observe(viewLifecycleOwner) { avatar ->
                    viewModel.registration(
                        login = login,
                        password = password,
                        name = name,
                        avatar = avatar.file

                    )

                }
                AndroidUtils.hideKeyboard(requireView())
            }
        }

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
                        binding.avatar.setImageURI(uri)
                        viewModel.changeAvatar(uri, uri?.toFile())
                    }
                }
            }


        binding.avatar.setOnClickListener {
            ImagePicker.with(this@RegistrationFragment)
                .crop()
                .compress(256)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            binding.progressRegistration.isVisible = state.logining
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_login, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isLogin.observe(viewLifecycleOwner) { state ->
            if (state) {
                findNavController().navigateUp()
            }
        }
        return binding.root
    }
}