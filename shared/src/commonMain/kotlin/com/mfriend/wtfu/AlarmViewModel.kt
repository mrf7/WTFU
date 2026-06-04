package com.mfriend.wtfu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val database: DatabaseHelper,
    private val alarmScheduler: AlarmScheduler,
) : ViewModel() {
    val alarmsFlow = database.getAlarms()

    fun getAlarm(id: Int): Flow<Alarm?> = database.getAlarm(id)

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            database.deleteAlarm(alarm)
            alarm.id?.let { alarmScheduler.cancel(it) }
        }
    }

    fun saveAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val saved = database.insertAlam(alarm.copy(enabled = true))
            alarmScheduler.schedule(saved)
        }
    }

    fun scheduleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val toSchedule = if (alarm.id == null) database.insertAlam(alarm) else alarm
            if (toSchedule.enabled) {
                alarmScheduler.schedule(toSchedule)
            }
        }
    }

    fun enableAlarm(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch {
            database.insertAlam(alarm.copy(enabled = enabled))
            if (enabled) {
                alarmScheduler.schedule(alarm)
            } else {
                alarm.id?.let { alarmScheduler.cancel(it) }
            }
        }
    }

}
