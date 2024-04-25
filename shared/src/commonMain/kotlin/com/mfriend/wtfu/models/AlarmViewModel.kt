package com.mfriend.wtfu.models

import com.mfriend.wtfu.Alarm
import com.mfriend.wtfu.DatabaseHelper
import com.mfriend.wtfu.RepeatMode
import kotlinx.coroutines.flow.map

class AlarmViewModel(private val database: DatabaseHelper) : ViewModel() {
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

    fun getAlarm(id: Int) = database.getAlarm(id)

    fun deleteAlarm(id: Long) {

    }

    fun saveAlarm(alarm: Alarm) {
//        viewModelScope.launch { database.insertAlam(alarm) }
    }
}