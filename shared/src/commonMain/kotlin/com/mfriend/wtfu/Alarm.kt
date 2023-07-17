package com.mfriend.wtfu

import kotlinx.datetime.DayOfWeek

data class Alarm(
    val hour: Int,
    val minute: Int,
    val repeat: RepeatMode,
    val enabled: Boolean= true,
)

sealed interface RepeatMode {
    object OneTime : RepeatMode
    object Weekdays : RepeatMode
    object Weekends : RepeatMode
    data class Custom(val days: List<DayOfWeek>) : RepeatMode
}