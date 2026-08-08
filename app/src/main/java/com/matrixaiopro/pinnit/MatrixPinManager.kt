package com.matrixaiopro.pinnit

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.matrixaiopro.R

class MatrixPinManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID = "matrix_pinnit_channel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Matrix Pin Notifications"
            val descriptionText = "Pinned notes for quick access"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun pinNote(id: Int, title: String, content: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Persistent
            .setAutoCancel(false)

        notificationManager.notify(id, builder.build())
    }

    fun updatePin(id: Int, title: String, content: String) {
        pinNote(id, title, content)
    }

    fun removePin(id: Int) {
        notificationManager.cancel(id)
    }
}
