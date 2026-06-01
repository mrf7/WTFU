package com.mfriend.wtfu

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AlarmBootReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            ACTION_QUICKBOOT_POWERON -> {
                scheduleDeferredRestore(context)
                restoreOnBackground(goAsync())
            }
            Intent.ACTION_USER_PRESENT,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                restoreOnBackground(goAsync())
            }
        }
    }

    private fun scheduleDeferredRestore(context: Context) {
        val appContext = context.applicationContext
        if (!AlarmNotificationPermissions.canScheduleExactAlarms(appContext)) {
            Log.w(TAG, "Cannot schedule deferred restore; exact alarm permission missing")
            return
        }
        val alarmManager = get<AlarmManager>()
        RESTORE_DELAYS_MS.forEachIndexed { index, delayMs ->
            val pendingIntent = PendingIntent.getBroadcast(
                appContext,
                RESTORE_REQUEST_CODE + index,
                Intent(appContext, AlarmReceiver::class.java).apply { action = AlarmReceiver.ACTION_RESTORE_ALARMS },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val triggerAt = System.currentTimeMillis() + delayMs
            try {
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, null), pendingIntent)
                Log.i(TAG, "Scheduled deferred alarm restore in ${delayMs / 1000}s")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to schedule deferred restore in ${delayMs / 1000}s", e)
            }
        }
    }

    private fun restoreOnBackground(pendingResult: PendingResult) {
        Thread {
            try {
                restoreEnabledAlarms()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore alarms", e)
            } finally {
                pendingResult.finish()
            }
        }.start()
    }

    private fun restoreEnabledAlarms() {
        runBlocking {
            val database = get<DatabaseHelper>()
            val scheduler = get<AlarmScheduler>()
            val alarms = database.getEnabledAlarms()
            Log.i(TAG, "Restoring ${alarms.size} enabled alarm(s)")
            alarms.forEach { scheduler.schedule(it) }
        }
    }

    companion object {
        private const val ACTION_QUICKBOOT_POWERON = "android.intent.action.QUICKBOOT_POWERON"
        private const val TAG = "AlarmBootReceiver"
        private const val RESTORE_REQUEST_CODE = 9_000_001
        private val RESTORE_DELAYS_MS = longArrayOf(30_000L, 180_000L)
    }
}
