package com.mfriend.wtfu

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
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
            Intent(Intent.ACTION_VIEW, "https://mrfiend.com/trigger/${alarm.id}".toUri(), context, MainActivity::class.java)
        val launch =
            PendingIntent.getActivity(context, 0, deepLinkIntent, PendingIntent.FLAG_IMMUTABLE)
        schedule(launch, alarm)
    }

    override fun scheduleNotification(alarm: Alarm) {
//        val intent =
//            Intent(Intent.ACTION_VIEW, "https://mrfiend.com/trigger/${alarm.id}".toUri(), context, MainActivity::class.java)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("EXTRA_ALARM", alarm.id?: error("Unsaved alarm"))
        }
        val broadcast = PendingIntent.getBroadcast(
            context,
            69,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        schedule(broadcast, alarm)
    }

    private fun schedule(intent: PendingIntent, alarm: Alarm) {
        // TODO figure out next alarm time
        // TODO This isnt giving the right time for some reason
        val time = System.currentTimeMillis() + 30_000
        alarmManager.setAlarmClock(AlarmClockInfo(time, intent), intent)
//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP,
//            time,
//            intent
//        )
        // this is in GMT, not sure if the time used by alarm manager cares about time zone
        Log.d("AlarmScheduler", "Scheduled alarm for ${Instant.fromEpochMilliseconds(time)}")
    }
}