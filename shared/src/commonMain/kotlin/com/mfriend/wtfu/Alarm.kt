package com.mfriend.wtfu

import kotlinx.datetime.DayOfWeek

data class Alarm(
    val hour: Int,
    val minute: Int,
    val repeat: RepeatMode,
    val id: Int? = null,
    val snooze: Int? = null,
    val enabled: Boolean = true,
    val missions: Mission? = null,
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
    object EveryDay : Custom(DayOfWeek.entries.toSet())
    open class Custom(val days: Set<DayOfWeek>) : RepeatMode
    companion object {
        fun fromDays(selectedDays: Set<DayOfWeek>) = when {
            selectedDays.isEmpty() -> OneTime
            selectedDays == Weekdays.days -> Weekdays
            selectedDays == Weekends.days -> Weekends
            selectedDays == EveryDay.days -> EveryDay
            else -> Custom(selectedDays)
        }
    }
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
