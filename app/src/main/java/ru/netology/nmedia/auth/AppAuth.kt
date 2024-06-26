package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor (
    @ApplicationContext
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authStateFlow = MutableStateFlow<Token?>(null)
    val authStateFlow = _authStateFlow.asStateFlow()

    val authSharedFlow = _authStateFlow.asSharedFlow()

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"

    }

    fun setAuth(id: Long, token: String) {
        _authStateFlow.value = Token(id, token)
        prefs.edit {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
        }
        sendPushToken()
    }

    fun clearAuth() {
        _authStateFlow.value = null
        prefs.edit { clear() }
        sendPushToken()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppEnteryPoint {
        fun  getApiService(): ApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                val enteryPoint = EntryPointAccessors.fromApplication(context, AppEnteryPoint::class.java)
                enteryPoint.getApiService().saveToken(pushToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id != 0L && token != null) {
            _authStateFlow.value = Token(id, token)
        } else {
            _authStateFlow.value = null
            prefs.edit {
                clear()
            }
        }
        sendPushToken()
    }


}