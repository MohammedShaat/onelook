package com.example.onelook.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.onelook.R

fun NotificationManager.sendNotification(context: Context, message: String, channelId: String) {

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setContentTitle(context.getString(R.string.notification_title))
        .setSmallIcon(R.drawable.ic_brand_logo)
        .setContentText(message)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notify(NOTIFICATION_ID, notificationBuilder.build())
}

fun getNotification(
    context: Context,
    message: String,
    channelId: String,
    intent: PendingIntent? = null
): Notification {

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setContentTitle(context.getString(R.string.notification_title))
        .setSmallIcon(R.drawable.ic_brand_logo)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(intent)

    return notificationBuilder.build()
}