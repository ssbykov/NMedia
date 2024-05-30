package ru.netology.nmedia.activity

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentLoginBinding
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.viewmodel.LoginViewModel
import ru.netology.nmedia.viewmodel.RegistrationViewModel

class RegistrationFragment : Fragment() {


    private val viewModel: RegistrationViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        with(binding) {
            registrationButton.setOnClickListener {
                viewModel.registration(
                    login = loginRegistrationEditText.text.toString(),
                    password = passwordRegistrationEditText.text.toString(),
                    name = nameRegistrationEditText.text.toString()
                )
            }
        }
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            binding.progressRegistration.isVisible = state.logining
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_login, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isLogin.observe(viewLifecycleOwner) {state->
            if (state) {
                findNavController().navigateUp()
            }
        }
        return binding.root
    }
}