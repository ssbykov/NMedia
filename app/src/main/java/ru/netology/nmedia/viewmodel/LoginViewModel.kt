package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.model.LoginState
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.utils.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: PostRepositoryImpl,
    private val appAuth: AppAuth
) : ViewModel() {

    private val _loginState = SingleLiveEvent<LoginState>()
    val loginState: LiveData<LoginState>
        get() = _loginState

    val isLogin =
        appAuth.authSharedFlow.map { it != null }.asLiveData(Dispatchers.Default)

    fun authentication(login: String, password: String) = viewModelScope.launch {
        try {
            _loginState.value = LoginState(logining = true)
            val token = repository.authentication(login, password)
            if (token != null) {
                appAuth.setAuth(token.id, token.token)
            }
            _loginState.value = LoginState()
        } catch (e: Exception) {
            _loginState.value = LoginState(error = true)
        }
    }

}