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
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val recipientId = "recipientId"
    private val gson = Gson()

    @Inject
    lateinit var appAuth: AppAuth

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
                    val notification = gson.fromJson(message.data[content], Like::class.java)
                    handleNotification(
                        notification,
                        stringRes = R.string.notification_user_liked,
                        fields = action.fields
                    )
                }

                Action.NEW_POST -> {
                    val notification = gson.fromJson(message.data[content], NewPost::class.java)
                    handleNotification(
                        notification,
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
            val userId = appAuth.authStateFlow.value?.id ?: 0L
            val notification = gson.fromJson(message.data[content], NewMailing::class.java)
            if (notification.recipientId == null || notification.recipientId == userId ||
                notification.recipientId == 0L && userId == 0L
            ) {
                handleNotification(
                    notification,
                    stringRes = R.string.new_notification,
                    fields = arrayOf(content)
                )
            } else appAuth.sendPushToken()
        }

    }

    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
    }

    private fun handleNotification(
        content: NewNotification,
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

