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
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import co.touchlab.kermit.Logger

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ALARM_FIRE) return
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        if (alarmId < 0) {
            Logger.withTag(TAG).w { "Alarm fired without valid id" }
            return
        }
        Logger.withTag(TAG).d { "Alarm fired for id $alarmId" }
        postAlarmNotification(context, alarmId)
    }

    private fun postAlarmNotification(context: Context, alarmId: Int) {
        if (!canPostNotifications(context)) {
            Logger.withTag(TAG).w { "POST_NOTIFICATIONS not granted" }
            return
        }
        createNotificationChannel(context)
        val contentIntent = PendingIntent.getActivity(
            context,
            alarmId,
            Intent(Intent.ACTION_VIEW, "https://mrfiend.com/$alarmId".toUri(), context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm")
            .setContentText("Time to wake up")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(contentIntent, true)
            .setContentIntent(contentIntent)
            .build()
        NotificationManagerCompat.from(context).notify(alarmId, notification)
    }

    private fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    private fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Alarm notifications"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setBypassDnd(true)
            setSound(
                android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ACTION_ALARM_FIRE = "com.mfriend.wtfu.action.ALARM_FIRE"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        private const val CHANNEL_ID = "alarm_channel"
        private const val TAG = "AlarmReceiver"
    }
}
