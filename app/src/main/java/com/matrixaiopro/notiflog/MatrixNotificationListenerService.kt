package com.matrixaiopro.notiflog

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.matrixaiopro.data.MatrixDatabase
import com.matrixaiopro.data.NotificationLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MatrixNotificationListenerService : NotificationListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.let {
            val packageName = it.packageName
            val extras = it.notification.extras
            val title = extras.getString("android.title")
            val text = extras.getCharSequence("android.text")?.toString()

            val log = NotificationLog(
                packageName = packageName,
                title = title,
                text = text
            )

            scope.launch {
                MatrixDatabase.getDatabase(applicationContext).matrixDao().insertNotificationLog(log)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Optional: logic when notification is removed
    }
}
