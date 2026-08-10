package com.blackbox.module.kai.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListenerController : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {}
    override fun onListenerConnected() {}
    override fun onListenerDisconnected() {}
}
