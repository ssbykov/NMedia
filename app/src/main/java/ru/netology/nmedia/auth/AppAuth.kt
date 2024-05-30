package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.dto.Token

class AppAuth private constructor(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _authStateFlow = MutableStateFlow<Token?>(null)
    val authStateFlow = _authStateFlow.asStateFlow()

    val authSharedFlow = _authStateFlow.asSharedFlow()

    companion object {
        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"

        private var INSTANCE: AppAuth? = null

        fun getInstance() = requireNotNull(INSTANCE) {
            "You must call init before"
        }

        fun initApp(context: Context) {
            INSTANCE = AppAuth(context.applicationContext)
        }
    }

    fun setAuth(id: Long, token: String) {
        _authStateFlow.value = Token(id, token)
        prefs.edit {
            putLong(ID_KEY, id)
            putString(TOKEN_KEY, token)
        }
    }

    fun clearAuth() {
        _authStateFlow.value = null
        prefs.edit { clear() }
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
    }


}