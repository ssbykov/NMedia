package ru.netology.nmedia


object Constants {

    private const val BASE_URL = "http://${BuildConfig.SERVER_IP}:9999"
    const val BASE_URL_AVATAR = "$BASE_URL/avatars/"
    const val BASE_URL_POST = "$BASE_URL/api/slow/posts"
    const val BASE_URL_SLOW = "$BASE_URL/api/slow/"
    const val BASE_URL_IMAGES = "$BASE_URL/media/"
    const val SDRF = "dd MMMM в H:mm"

    const val KEY_CONTENT = "KEY_CONTENT"
    const val KEY_ATTACHMENT = "KEY_ATTACHMENT"
}