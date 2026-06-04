package com.mfriend.wtfu

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.adapter.primitive.IntColumnAdapter
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.SqlDriver
import com.mfriend.AlarmDb
import com.mfriend.AlarmDbo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek

private object RepeatModeAdapter : ColumnAdapter<RepeatMode, String> {
    override fun decode(databaseValue: String): RepeatMode =
        if (databaseValue.isBlank()) {
            RepeatMode.OneTime
        } else {
            val days = databaseValue.split(",").map { DayOfWeek.valueOf(it) }.toSet()
            RepeatMode.fromDays(days)
        }


    override fun encode(value: RepeatMode): String =
        if (value is RepeatMode.Custom) {
            value.days.joinToString(",")
        } else {
            ""
        }
}


private object MissionsAdapter : ColumnAdapter<Mission, String> {
    override fun decode(databaseValue: String): Mission = MathMission()

    override fun encode(value: Mission): String = "MATH"

}

class DatabaseHelper(driver: SqlDriver) {
    private val database = AlarmDb(
        driver, AlarmDbo.Adapter(
            IntColumnAdapter, IntColumnAdapter, IntColumnAdapter,
            repeatAdapter = RepeatModeAdapter,
            missionsAdapter = MissionsAdapter
        )
    )
    private val alarmQueries = database.alarmQueries
    fun getAlarms(): Flow<List<Alarm>> {
        return alarmQueries.selectAll().asFlow().mapToList(Dispatchers.IO).map { it.map { it.toAlarm() } }
    }

    fun getAlarm(id: Int): Flow<Alarm?> {
        return alarmQueries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toAlarm() }
    }

    suspend fun getEnabledAlarms(): List<Alarm> = withContext(Dispatchers.IO) {
        alarmQueries.selectAll().executeAsList().map { it.toAlarm() }.filter { it.enabled }
    }

    suspend fun insertAlam(alarm: Alarm): Alarm {
        return withContext(Dispatchers.IO) {
            alarmQueries.insert(
                alarm.id,
                alarm.hour,
                alarm.minute,
                alarm.enabled,
                alarm.sound,
                alarm.repeat,
                alarm.missions
            ).executeAsOne().toAlarm()
        }
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        withContext(Dispatchers.IO) {
            alarm.id?.let { alarmQueries.delete(it) }
        }
    }
}

internal fun AlarmDbo.toAlarm() =
    Alarm(
        hour = hour,
        minute = minute,
        repeat = repeat,
        id = id,
        snooze = null,
        enabled = enabled,
        sound = sound,
        missions = missions
    )