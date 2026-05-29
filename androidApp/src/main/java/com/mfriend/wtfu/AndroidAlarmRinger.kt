package com.mfriend.wtfu

import android.content.Context
import android.content.Intent

class AndroidAlarmRinger(context: Context) : AlarmRinger {
    private val appContext = context.applicationContext

    override fun start(alarmId: Int) {
        val intent = Intent(appContext, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_START
            putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
        }
        appContext.startForegroundService(intent)
    }

    override fun stop(alarmId: Int) {
        val intent = Intent(appContext, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_STOP
            putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
        }
        appContext.startService(intent)
    }
}
