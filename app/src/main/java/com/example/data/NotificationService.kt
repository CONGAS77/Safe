package com.example.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.domain.model.NotificationCategory
import com.example.domain.model.SafeNotification
import java.util.UUID

class NotificationService(private val context: Context? = null) {
    private val channelId = "safe_alerts_channel"

    init {
        ensureChannel()
    }

    private fun ensureChannel() {
        val ctx = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channel = NotificationChannel(
                channelId,
                "SAFE Alerts",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Notifications de sessions et check-ins SAFE"
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun createNotification(
        category: NotificationCategory,
        title: String,
        body: String,
    ): SafeNotification {
        trySendNativeNotification(title, body)
        return SafeNotification(
            id = "notif-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(4)}",
            category = category,
            title = title,
            body = body,
            createdAt = System.currentTimeMillis(),
            read = false,
        )
    }

    fun trySendNativeNotification(title: String, body: String) {
        val ctx = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        try {
            val notification = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(ctx).notify(System.currentTimeMillis().toInt(), notification)
        } catch (_: SecurityException) {
            // Ignored if permission revoked
        }
    }
}
