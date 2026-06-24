package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reforge Protocol Alert"
        val text = intent.getStringExtra("text") ?: "Stay focused on your blueprint."
        val type = intent.getStringExtra("type") ?: "Action" // Preparation, Action, Recovery, Risk, Identity
        val notificationId = intent.getIntExtra("id", 1001)

        Log.d("NotificationReceiver", "Received broadcast: title=$title, text=$text, type=$type, id=$notificationId")

        showNotification(context, title, text, type, notificationId)
    }

    private fun showNotification(context: Context, title: String, text: String, type: String, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reforge_protocol_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Reforge Protocol Alerts"
            val channelDescription = "Local notifications for your daily lifestyle reforge schedule"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action intent to launch the app
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Decorate with specific icons/prefixes based on notification type
        val typePrefix = when (type) {
            "Preparation" -> "⏳ [PREP] "
            "Action" -> "⚡ [ACTION] "
            "Recovery" -> "🌱 [RECOVERY] "
            "Risk" -> "⚠️ [RISK ALERT] "
            "Identity" -> "🌌 [IDENTITY] "
            else -> "🔔 "
        }

        val decoratedTitle = typePrefix + title

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard system icon fallback
            .setContentTitle(decoratedTitle)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(
                when (type) {
                    "Risk" -> NotificationCompat.CATEGORY_ALARM
                    "Preparation" -> NotificationCompat.CATEGORY_EVENT
                    else -> NotificationCompat.CATEGORY_STATUS
                }
            )

        notificationManager.notify(notificationId, builder.build())
    }
}
