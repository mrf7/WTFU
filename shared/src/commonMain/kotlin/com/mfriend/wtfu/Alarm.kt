package com.mfriend.wtfu

import kotlinx.datetime.DayOfWeek

data class Alarm(
    val hour: Int,
    val minute: Int,
    val repeat: RepeatMode,
    val id: Int? = null,
    val snooze: Int? = null,
    val enabled: Boolean = true,
    val missions: List<Mission> = emptyList(),
    val sound: String = "random"
)

sealed interface Mission {
    val name: String
    val icon: Int
}

class MathMission : Mission {
    override val name = "Math"
    override val icon = 0
}

sealed interface RepeatMode {
    object OneTime : RepeatMode
    object Weekdays : Custom(
        setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    )

    object Weekends : Custom(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
    object EveryDay : Custom(DayOfWeek.values().toSet())
    open class Custom(val days: Set<DayOfWeek>) : RepeatMode
}

fun RepeatMode.localizeString(): String {
    val repeatText = when (this) {
        RepeatMode.Weekdays -> "Weekdays"
        RepeatMode.Weekends -> "Weekends"
        RepeatMode.EveryDay -> "Every Day"
        RepeatMode.OneTime -> "One Time"
        is RepeatMode.Custom -> this.days.joinToString { it.name.take(3) }
    }
    return repeatText
}
