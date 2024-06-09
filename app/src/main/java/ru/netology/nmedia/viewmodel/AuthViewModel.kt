package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.di.DependencyContainer

class AuthViewModel : ViewModel() {
    val auth = DependencyContainer.getInstance().appAuth
        .authStateFlow
        .asLiveData()
}