package com.mfriend.wtfu

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

class AndroidAlarmScheduler(
    private val alarmManager: AlarmManager,
    context: Context,
) : AlarmScheduler {
    private val context = context.applicationContext
    private val log = Logger.withTag("AndroidAlarmScheduler")

    override fun schedule(alarm: Alarm) {
        val alarmId = alarm.id ?: return
        if (!AlarmNotificationPermissions.canScheduleExactAlarms(context)) {
            log.w { "Cannot schedule alarm $alarmId; exact alarm permission missing" }
            return
        }
        val triggerAtMillis = triggerAtMillis(alarm)
        if (triggerAtMillis == null) {
            log.d { "Skipping schedule for alarm $alarmId (no next trigger)" }
            return
        }
        cancel(alarmId)
        val operation = alarmOperationPendingIntent(alarmId) ?: return
        val showIntent = showActivityPendingIntent(alarmId)
        val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent)
        try {
            alarmManager.setAlarmClock(info, operation)
            log.d { "Scheduled alarm $alarmId at $triggerAtMillis (${Instant.fromEpochMilliseconds(triggerAtMillis)})" }
        } catch (e: SecurityException) {
            log.e(e) { "Failed to schedule alarm $alarmId" }
        }
    }

    override fun cancel(alarmId: Int) {
        alarmOperationPendingIntent(alarmId, create = false)?.let { operation ->
            alarmManager.cancel(operation)
            operation.cancel()
        }
        showActivityPendingIntent(alarmId, create = false)?.cancel()
        log.d { "Cancelled alarm $alarmId" }
    }

    private fun triggerAtMillis(alarm: Alarm): Long? {
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        return nextAlarmTriggerAt(alarm, now, TimeZone.currentSystemDefault())?.toEpochMilliseconds()
    }

    private fun alarmOperationPendingIntent(alarmId: Int, create: Boolean = true): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_FIRE
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val flags = PendingIntent.FLAG_IMMUTABLE or
            if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getBroadcast(context, alarmId, intent, flags)
    }

    private fun showActivityPendingIntent(alarmId: Int, create: Boolean = true): PendingIntent? {
        val intent = alarmDeepLinkIntent(alarmId)
        val flags = PendingIntent.FLAG_IMMUTABLE or
            if (create) PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_NO_CREATE
        return PendingIntent.getActivity(context, showRequestCode(alarmId), intent, flags)
    }

    private fun alarmDeepLinkIntent(alarmId: Int): Intent =
        Intent(Intent.ACTION_VIEW, "https://mrfiend.com/$alarmId".toUri(), context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

    private fun showRequestCode(alarmId: Int) = alarmId + SHOW_REQUEST_CODE_OFFSET

    companion object {
        private const val SHOW_REQUEST_CODE_OFFSET = 1_000_000
    }
}
