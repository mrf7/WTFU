package com.mfriend.wtfu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AlarmViewModel(private val database: DatabaseHelper, private val alarmScheduler: AlarmScheduler) : ViewModel() {
    val alarmsFlow = database.getAlarms().map { alarms ->
        alarms.map {
            Alarm(
                hour = it.hour,
                minute = it.minute,
                repeat = RepeatMode.OneTime,
                enabled = it.enabled,
                sound = it.sound,
                id = it.id
            )
        }
    }

    fun getAlarm(id: Int): Flow<Alarm?> {
        return database.getAlarm(id).map { it?.toAlarm() }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            database.deleteAlarm(alarm)
        }
    }

    fun saveAlarm(alarm: Alarm) {
        viewModelScope.launch { database.insertAlam(alarm) }
    }

    fun scheduleAlarm(alarm: Alarm) {
        saveAlarm(alarm)
       alarmScheduler.scheduleNotification(alarm)
    }
}