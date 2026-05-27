package com.mfriend.wtfu

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun nextAlarmTriggerAt(alarm: Alarm, now: Instant, timeZone: TimeZone): Instant? {
    if (!alarm.enabled || alarm.id == null) return null

    return when (val repeat = alarm.repeat) {
        RepeatMode.OneTime -> nextOneTimeTrigger(alarm.hour, alarm.minute, now, timeZone)
        is RepeatMode.Custom -> nextRepeatingTrigger(alarm.hour, alarm.minute, repeat.days, now, timeZone)
    }
}

private fun nextOneTimeTrigger(
    hour: Int,
    minute: Int,
    now: Instant,
    timeZone: TimeZone,
): Instant? {
    val today = now.toLocalDateTime(timeZone).date
    val todayTrigger = localDateTimeAt(today, hour, minute).toInstant(timeZone)
    if (todayTrigger > now) return todayTrigger
    val tomorrow = today.plus(1, DateTimeUnit.DAY)
    return localDateTimeAt(tomorrow, hour, minute).toInstant(timeZone)
}

private fun nextRepeatingTrigger(
    hour: Int,
    minute: Int,
    days: Set<DayOfWeek>,
    now: Instant,
    timeZone: TimeZone,
): Instant? {
    val startDate = now.toLocalDateTime(timeZone).date
    repeat(8) { offset ->
        val date = startDate.plus(offset, DateTimeUnit.DAY)
        if (date.dayOfWeek !in days) return@repeat
        val trigger = localDateTimeAt(date, hour, minute).toInstant(timeZone)
        if (trigger > now) return trigger
    }
    return null
}

private fun localDateTimeAt(date: LocalDate, hour: Int, minute: Int): LocalDateTime =
    LocalDateTime(date.year, date.month, date.day, hour, minute)
