package com.mfriend.wtfu.android

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mfriend.wtfu.Alarm
import com.mfriend.wtfu.AlarmScheduler
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

class AndroidAlarmScheduler(private val alarmManager: AlarmManager, private val context: Context) :
    AlarmScheduler {
    override fun scheduleLaunch(alarm: Alarm) {
        // TODO Set up deep link into alarm screen once the screen is setup
        val actIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("ID", alarm.id ?: -1)
        }
        val launch =
            PendingIntent.getActivity(context, 0, actIntent, PendingIntent.FLAG_IMMUTABLE)
        schedule(launch, alarm)
    }

    override fun scheduleNotification(alarm: Alarm) {
        val intent =
            Intent(context, AlarmReceiver::class.java).apply { putExtra("ID", alarm.id ?: -1) }
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
        val time = Clock.System.now().plus(15.seconds).epochSeconds
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            intent
        )
    }
}