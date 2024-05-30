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
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.viewmodel.LoginViewModel

class LoginFragment : Fragment() {


    private val viewModel: LoginViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)

        with(binding) {
            loginButton.setOnClickListener {
                viewModel.login(
                    login = loginEditText.text.toString(),
                    password = passwordEditText.text.toString()
                )
            }
        }
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            binding.progressLogin.isVisible = state.logining
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.isLogin.observe(viewLifecycleOwner){
            if (it != null) {
                findNavController().navigateUp()
            }
        }
        return binding.root
    }
}