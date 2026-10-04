package com.dynamicdevelopers.reslink.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.SystemClock
import android.util.Log
import com.dynamicdevelopers.reslink.ResLinkApp
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ResLinkFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "FCM token refreshed")
        val app = application as? ResLinkApp
        app?.uploadPushToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: "ResLink"
        val body = message.notification?.body ?: message.data["body"].orEmpty()

        if (body.isBlank()) return

        val channelId = "reslink_alerts"
        val manager = getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                "ResLink alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
        )

        val notification = Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .apply {
                packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
                    setContentIntent(
                        PendingIntent.getActivity(
                            this@ResLinkFirebaseMessagingService,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT or
                                    PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }
            }
            .build()

        try {
            manager.notify(
                SystemClock.elapsedRealtime().toInt(),
                notification
            )
        } catch (exception: SecurityException) {
            Log.w("FCM", "Notification permission is not granted")
        }
    }
}