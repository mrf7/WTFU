package com.mfriend.wtfu

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import co.touchlab.kermit.Logger
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlin.time.Instant

class AndroidAlarmScheduler(
    private val alarmManager: AlarmManager,
    private val context: Context,
) : AlarmScheduler {
    private val log = Logger.withTag("AndroidAlarmScheduler")

    override fun schedule(alarm: Alarm) {
        val alarmId = alarm.id ?: return
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val triggerAt = nextAlarmTriggerAt(alarm, now, TimeZone.currentSystemDefault())
        if (triggerAt == null) {
            log.d { "Skipping schedule for alarm $alarmId (no next trigger)" }
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            log.w { "Cannot schedule exact alarm $alarmId: permission denied" }
            return
        }
        val pendingIntent = alarmPendingIntent(alarmId) ?: return
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt.toEpochMilliseconds(),
            pendingIntent,
        )
        log.d { "Scheduled alarm $alarmId for $triggerAt" }
    }

    override fun cancel(alarmId: Int) {
        val pendingIntent = alarmPendingIntent(alarmId, create = false) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        log.d { "Cancelled alarm $alarmId" }
    }

    private fun alarmPendingIntent(alarmId: Int, create: Boolean = true): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val flags = PendingIntent.FLAG_IMMUTABLE or
                if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, alarmId, intent, flags)
    }
}
