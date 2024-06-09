package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.repository.PostRepositoryImpl

class ViewModelFactory(
    private val repository: PostRepositoryImpl,
    private val appAuth: AppAuth
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(PostViewModel::class.java) -> {
                PostViewModel(repository, appAuth) as T
            }

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository, appAuth) as T
            }

            modelClass.isAssignableFrom(RegistrationViewModel::class.java) -> {
                RegistrationViewModel(repository, appAuth) as T
            }

            else -> error("Unknown class: $modelClass")
        }
}