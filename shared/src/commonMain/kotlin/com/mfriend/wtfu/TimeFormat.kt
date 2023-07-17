package com.mfriend.wtfu

// TODO replace this with something that isnt dumb
fun Alarm.toTimeString() = "$hour:${if (minute < 10) "0$minute" else minute}"