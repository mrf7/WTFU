package com.mfriend.wtfu

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class AlarmScheduleTimeTest : FunSpec({
    val timeZone = TimeZone.of("America/New_York")

    test("returns null when disabled") {
        val alarm = alarmAt(7, 0, id = 1, enabled = false)
        val now = instant(2026, 5, 27, 6, 0, timeZone)
        nextAlarmTriggerAt(alarm, now, timeZone) shouldBe null
    }

    test("returns null when id missing") {
        val alarm = alarmAt(7, 0, id = null)
        val now = instant(2026, 5, 27, 6, 0, timeZone)
        nextAlarmTriggerAt(alarm, now, timeZone) shouldBe null
    }

    test("one time today when still in future") {
        val alarm = alarmAt(7, 30, id = 1)
        val now = instant(2026, 5, 27, 6, 0, timeZone)
        val expected = instant(2026, 5, 27, 7, 30, timeZone)
        nextAlarmTriggerAt(alarm, now, timeZone) shouldBe expected
    }

    test("one time tomorrow when time passed today") {
        val alarm = alarmAt(7, 0, id = 1)
        val now = instant(2026, 5, 27, 8, 0, timeZone)
        val expected = instant(2026, 5, 28, 7, 0, timeZone)
        nextAlarmTriggerAt(alarm, now, timeZone) shouldBe expected
    }

    test("weekdays skips to next matching day") {
        val alarm = alarmAt(7, 0, id = 1, repeat = RepeatMode.Weekdays)
        // Tuesday 2026-05-27
        val now = instant(2026, 5, 27, 8, 0, timeZone)
        val expected = instant(2026, 5, 28, 7, 0, timeZone) // Wednesday
        nextAlarmTriggerAt(alarm, now, timeZone) shouldBe expected
    }

    test("every day uses tomorrow when today passed") {
        val alarm = alarmAt(9, 0, id = 1, repeat = RepeatMode.EveryDay)
        val now = instant(2026, 5, 27, 10, 0, timeZone)
        val expected = instant(2026, 5, 28, 9, 0, timeZone)
        nextAlarmTriggerAt(alarm, now, timeZone) shouldBe expected
    }
})

private fun alarmAt(
    hour: Int,
    minute: Int,
    id: Int? = 1,
    enabled: Boolean = true,
    repeat: RepeatMode = RepeatMode.OneTime,
) = Alarm(hour = hour, minute = minute, repeat = repeat, id = id, enabled = enabled)

private fun instant(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int = 0,
    timeZone: TimeZone,
): Instant = LocalDateTime(year, month, day, hour, minute).toInstant(timeZone)
