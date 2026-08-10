package com.blackbox.module.kai.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class KaiNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {}
    override fun onListenerDisconnected() {}
    override fun onNotificationPosted(sbn: StatusBarNotification) {}
}
