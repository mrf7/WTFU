package com.mfriend.wtfu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.touchlab.kermit.Logger
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext

class AlarmBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RESCHEDULE_ACTIONS) return
        val pendingResult = goAsync()
        runBlocking {
            try {
                val koin = GlobalContext.get()
                val database = koin.get<DatabaseHelper>()
                val scheduler = koin.get<AlarmScheduler>()
                database.getEnabledAlarms().forEach { scheduler.schedule(it) }
                Logger.withTag(TAG).d { "Rescheduled enabled alarms after ${intent.action}" }
            } catch (e: Exception) {
                Logger.withTag(TAG).e(e) { "Failed to reschedule alarms after ${intent.action}" }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "AlarmBootReceiver"

        private val RESCHEDULE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
        )
    }
}
