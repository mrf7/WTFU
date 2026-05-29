package com.mfriend.wtfu

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-posts the ringing notification if the user dismisses it from the shade.
 * While the foreground service is active, the alarm must stay reachable via notification.
 */
class AlarmRingingNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmRingingService.ACTION_NOTIFICATION_DISMISSED) return
        val alarmId = intent.getIntExtra(AlarmRingingService.EXTRA_ALARM_ID, -1)
        if (alarmId < 0) return
        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = AlarmRingingService.ACTION_REPUBLISH_NOTIFICATION
            putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarmId)
        }
        context.startService(serviceIntent)
    }
}
