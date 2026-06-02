package com.mfriend.wtfu

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

class AlarmTriggerViewModel(
    private val database: DatabaseHelper,
    private val alarmRinger: AlarmRinger,
) : ViewModel() {
    fun getAlarm(id: Int): Flow<Alarm?> = database.getAlarm(id)

    fun stopRinging(alarmId: Int) {
        alarmRinger.stop(alarmId)
    }
}