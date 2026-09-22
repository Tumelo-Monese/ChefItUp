package com.chefitup.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.chefitup.app.R

object CookingTimerNotifier {
    const val CHANNEL_ID = "cooking_timers"
    private const val NOTIFICATION_ID = 4201

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.cooking_timer),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.cooking_timer_done_body)
        }
        manager.createNotificationChannel(channel)
    }

    fun notifyComplete(context: Context) {
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chef_logo)
            .setContentTitle(context.getString(R.string.cooking_timer_done_title))
            .setContentText(context.getString(R.string.cooking_timer_done_body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
}
