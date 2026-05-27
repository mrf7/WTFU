package com.mfriend.wtfu

import android.app.ActivityOptions
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.os.Build
import android.util.Log
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
        postAlarmNotification(context.applicationContext, alarmId)
    }

    private fun postAlarmNotification(context: Context, alarmId: Int) {
        if (!AlarmNotificationPermissions.canPostNotifications(context)) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted")
            return
        }
        createNotificationChannel(context)
        val contentIntent = alarmContentPendingIntent(context, alarmId)
        val fullScreenAllowed = AlarmNotificationPermissions.canUseFullScreenIntent(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm")
            .setContentText("Time to wake up")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
            .apply {
                if (fullScreenAllowed) {
                    setFullScreenIntent(contentIntent, true)
                }
            }
            .build()
        try {
            NotificationManagerCompat.from(context).notify(alarmId, notification)
            Log.i(TAG, "notified alarm $alarmId fullScreen=$fullScreenAllowed")
        } catch (e: SecurityException) {
            Log.e(TAG, "notify failed", e)
            launchAlarmUi(context, contentIntent)
            return
        }
        if (!fullScreenAllowed) {
            Log.w(TAG, "full-screen intent disabled; launching activity")
            launchAlarmUi(context, contentIntent)
        }
    }

    private fun launchAlarmUi(context: Context, contentIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic().apply {
                    @Suppress("DEPRECATION")
                    pendingIntentBackgroundActivityStartMode =
                        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                }
                contentIntent.send(options.toBundle())
            } else {
                contentIntent.send()
            }
            Log.i(TAG, "launched alarm UI")
        } catch (e: PendingIntent.CanceledException) {
            Log.e(TAG, "launch failed", e)
        }
    }

    private fun alarmContentPendingIntent(context: Context, alarmId: Int): PendingIntent {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://mrfiend.com/$alarmId".toUri(),
            context,
            MainActivity::class.java,
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

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
