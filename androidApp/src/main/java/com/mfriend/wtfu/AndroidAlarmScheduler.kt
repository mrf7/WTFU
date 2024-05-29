package com.mfriend.wtfu

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import kotlinx.datetime.Instant

class AndroidAlarmScheduler(private val alarmManager: AlarmManager, private val context: Context) :
    AlarmScheduler {
    override fun scheduleLaunch(alarm: Alarm) {
        val deepLinkIntent =
            Intent(Intent.ACTION_VIEW, "https://mrfiend.com/${alarm.id}".toUri(), context, MainActivity::class.java)
        val launch =
            PendingIntent.getActivity(context, 0, deepLinkIntent, PendingIntent.FLAG_IMMUTABLE)
        schedule(launch, alarm)
    }

    override fun scheduleNotification(alarm: Alarm) {
        val intent =
            Intent(Intent.ACTION_VIEW, "https://mrfiend.com/${alarm.id}".toUri(), context, MainActivity::class.java)
        val broadcast = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        schedule(broadcast, alarm)
    }

    private fun schedule(intent: PendingIntent, alarm: Alarm) {
        // TODO figure out next alarm time
        // TODO This isnt giving the right time for some reason
        val time = System.currentTimeMillis() + 15_000
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            intent
        )
        Log.d("AlarmScheduler", "Scheduled alarm for ${Instant.fromEpochMilliseconds(time)}")
    }
}