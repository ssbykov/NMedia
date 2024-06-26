package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.model.LoginState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.utils.SingleLiveEvent
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val repository: PostRepositoryImpl,
    private val appAuth: AppAuth
) : ViewModel() {

    private val _loginState = SingleLiveEvent<LoginState>()
    val loginState: LiveData<LoginState>
        get() = _loginState

    private val noAvatar = PhotoModel()
    private val _avatar = MutableLiveData(noAvatar)
    val avatar: LiveData<PhotoModel>
        get() = _avatar

    fun changeAvatar(uri: Uri?, file: File? = null) {
        _avatar.value = PhotoModel(uri, file)
    }

    fun dropAvatar() {
        _avatar.value = PhotoModel()
    }

    val isLogin =
        appAuth.authSharedFlow.map { it != null }.asLiveData(Dispatchers.Default)

    fun registration(login: String, password: String, name: String, avatar: File? = null) =
        viewModelScope.launch {
            try {
                _loginState.value = LoginState(logining = true)
                val token = if (avatar != null) {
                    repository.registerWithPhoto(login, password, name, avatar)
                } else {
                    repository.registration(login, password, name)
                }
                if (token != null) {
                    appAuth.setAuth(token.id, token.token)
                }
                _loginState.value = LoginState()
            } catch (e: Exception) {
                _loginState.value = LoginState(error = true)
            }
        }

}