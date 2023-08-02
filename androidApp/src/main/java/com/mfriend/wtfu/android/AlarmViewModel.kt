package com.mfriend.wtfu.android

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.mfriend.wtfu.Alarm

class AlarmViewModel : ViewModel() {
    val alarms = mutableStateListOf<Alarm>(sampleAlarms.first())

    fun getAlarm(id: Int) = alarms.getOrNull(id)

    fun addAlarm(alarm: Alarm) {
        alarms.add(alarm)
        Log.d("Mrf", "Added alarm: ${alarms.joinToString()}")
    }

    fun updateAlarm(id: Int, alarm: Alarm) {
        alarms[id] = alarm
    }
}