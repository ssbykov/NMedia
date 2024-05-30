package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.model.LoginState
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.utils.SingleLiveEvent

class RegistrationViewModel (application: Application) : AndroidViewModel(application) {

    private val repository = PostRepositoryImpl(AppDb.getInstance(application).postDao())

    private val _loginState = SingleLiveEvent<LoginState>()
    val loginState: LiveData<LoginState>
        get() = _loginState

    val isLogin =
        AppAuth.getInstance().authSharedFlow.map { it != null }.asLiveData(Dispatchers.Default)

    fun registration(login: String, password: String, name: String) = viewModelScope.launch {
        try {
            _loginState.value = LoginState(logining = true)
            val token = repository.registration(login, password, name)
            if (token != null) {
                AppAuth.getInstance().setAuth(token.id, token.token)
            }
            _loginState.value = LoginState()
        } catch (e: Exception) {
            _loginState.value = LoginState(error = true)
        }
    }

}