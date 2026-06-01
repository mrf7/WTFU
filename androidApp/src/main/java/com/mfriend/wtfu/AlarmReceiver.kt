package com.mfriend.wtfu

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_RESTORE_ALARMS -> {
                val pendingResult = goAsync()
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
            ACTION_ALARM_FIRE -> handleAlarmFire(context, intent)
        }
    }

    private fun handleAlarmFire(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        if (alarmId < 0) {
            Logger.withTag(TAG).w { "Alarm fired without valid id" }
            return
        }
        Logger.withTag(TAG).d { "Alarm fired for id $alarmId" }

        val appContext = context.applicationContext
        get<AlarmRinger>().start(alarmId)
        rescheduleAlarm(alarmId)
        launchAlarmUi(alarmContentPendingIntent(appContext, alarmId))
    }

    private fun rescheduleAlarm(alarmId: Int) {
        val database = get<DatabaseHelper>()
        val scheduler = get<AlarmScheduler>()
        runBlocking {
            val alarm = database.getAlarm(alarmId).first() ?: return@runBlocking
            when (alarm.repeat) {
                RepeatMode.OneTime -> {
                    scheduler.cancel(alarmId)
                    if (alarm.enabled) {
                        database.insertAlam(alarm.copy(enabled = false))
                    }
                }
                else -> if (alarm.enabled) scheduler.schedule(alarm)
            }
        }
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

    private fun launchAlarmUi(contentIntent: PendingIntent) {
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

    companion object {
        const val ACTION_ALARM_FIRE = "com.mfriend.wtfu.action.ALARM_FIRE"
        const val ACTION_RESTORE_ALARMS = "com.mfriend.wtfu.action.RESTORE_ALARMS"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        private const val TAG = "AlarmReceiver"
    }
}
