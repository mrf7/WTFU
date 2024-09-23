package com.mfriend.wtfu

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Right now this only shows as a regular notification if youre looking at your phone so
        // you can just keep using your phone if
        createNotificationChannel(context)
        val alarmId = intent.getIntExtra("EXTRA_ALARM", -1)
        val fullScreenIntent = Intent(Intent.ACTION_VIEW, "https://mrfiend.com/trigger/${alarmId}".toUri(), context, MainActivity::class.java)

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, 0,
            fullScreenIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "ID")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("My notification")
            .setContentText("Hello World!")
            .setAutoCancel(true)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .build()

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define.
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("MRF", "No perms")
                return
            }
            Log.d("MRF", "Notifying")
            notify(3, builder)
        }
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        val name = "Bing"
        val descriptionText = "Bing bong ring rong"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("ID", name, importance).apply {
            description = descriptionText
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}