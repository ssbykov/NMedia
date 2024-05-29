package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import ru.netology.nmedia.auth.AppAuth

class AuthViewModel : ViewModel() {
    val auth = AppAuth.getInstance()
        .authStateFlow
        .asLiveData()

    val isAuthenticated: Boolean
//        get() = auth.value?.token != null
        get() {
            println("authStateFlow: isAuthenticated - ${auth.value?.token}")
            return auth.value?.token != null
        }

}