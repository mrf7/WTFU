package com.mfriend.wtfu

fun Alarm.toTimeString(): String {
    val minutePart = minute.toString().padStart(2, '0')
    val displayHour = when (val h = hour % 12) {
        0 -> 12
        else -> h
    }
    val amPm = if (hour < 12) "AM" else "PM"
    return "$displayHour:$minutePart $amPm"
}
