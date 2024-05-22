package ru.netology.nmedia.error

import okio.IOException
import java.sql.SQLException

sealed class AppError(var code: String): RuntimeException() {
    companion object {
        fun from(e: Throwable) : AppError = when(e) {
            is ApiError -> e
            is IOException -> NetworkError
            is SQLException -> DbError
            else -> UnknownError
        }
    }
}
class ApiError(val status: Int, code: String): AppError(code)
object NetworkError : AppError("error_network")
object DbError : AppError("error_db")
object UnknownError: AppError("error_unknown")