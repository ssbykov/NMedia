package ru.netology.nmedia.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val recipientId = "recipientId"
    private val gson = Gson()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        message.data[action]?.let {
            if (it !in Action.entries.toString()) {
                return@let
            }
            when (val action = Action.valueOf(it)) {
                Action.LIKE -> {
                    val content = gson.fromJson(message.data[content], Like::class.java)
                    handleNotification(
                        content,
                        stringRes = R.string.notification_user_liked,
                        fields = action.fields
                    )
                }

                Action.NEW_POST -> {
                    val content = gson.fromJson(message.data[content], Like::class.java)
                    handleNotification(
                        content,
                        stringRes = R.string.notification_user_new_post,
                        fields = action.fields
                    )
                }
            }
        }
        message.data[content]?.let {
            if (recipientId !in it) {
                return@let
            }
            val userId = AppAuth.getInstance().authStateFlow.value?.id
            val content = gson.fromJson(message.data[content], NewNotification::class.java)
            if (content.recipientId == null && content.recipientId == userId) {
                handleNotification(
                    content,
                    stringRes = R.string.new_notification,
                    fields = arrayOf("content")
                )
            } else AppAuth.getInstance().sendPushToken()
        }

    }

    override fun onNewToken(token: String) {
        AppAuth.getInstance().sendPushToken(token)
    }

    private fun handleNotification(
        content: Any,
        stringRes: Int,
        smallIconRes: Int = R.drawable.ic_notification,
        fields: Array<String>
    ) {

        val strings = content.javaClass.declaredFields.filter {
            it.isAccessible = true
            it.name in fields
        }.map { it.get(content) }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(smallIconRes)
            .setContentTitle(
                getString(
                    stringRes,
                    *strings.toTypedArray()
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .also {
                if (content is NewPost) {
                    it.setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("${content.content.substring(0, 100)}...")
                    )
                }
            }
            .build()

        notify(notification)
    }

    private fun notify(notification: Notification) {
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this)
                .notify(Random.nextInt(100_000), notification)
        }
    }

}

