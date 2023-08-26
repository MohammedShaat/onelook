package com.example.onelook.common.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.onelook.R

fun NotificationManager.sendNotification(
    context: Context,
    id: Int,
    message: String,
    channelId: String,
    pendingIntent: PendingIntent? = null
) {

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setContentTitle(context.getString(R.string.notification_title))
        .setSmallIcon(R.drawable.ic_brand_logo)
        .setContentText(message)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)

    notify(id, notificationBuilder.build())
}

fun getNotification(
    context: Context,
    message: String,
    channelId: String,
    pendingIntent: PendingIntent? = null
): Notification {

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setContentTitle(context.getString(R.string.notification_title))
        .setSmallIcon(R.drawable.ic_brand_logo)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setContentIntent(pendingIntent)

    return notificationBuilder.build()
}