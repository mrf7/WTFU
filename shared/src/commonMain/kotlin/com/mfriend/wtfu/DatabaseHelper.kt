package com.mfriend.wtfu

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

class DatabaseHelper(driver: SqlDriver) {
    private val database = AlarmDb(driver, AlarmDbo.Adapter(IntColumnAdapter, IntColumnAdapter, IntColumnAdapter))
    private val alarmQueries = database.alarmQueries
    fun getAlarms(): Flow<List<AlarmDbo>> {
        return alarmQueries.selectAll().asFlow().mapToList(Dispatchers.IO)
    }

    fun getAlarm(id: Int): Flow<Alarm?> {
        return alarmQueries.selectById(id).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toAlarm() }
    }

    suspend fun getEnabledAlarms(): List<Alarm> = withContext(Dispatchers.IO) {
        alarmQueries.selectAll().executeAsList().map { it.toAlarm() }.filter { it.enabled }
    }

    suspend fun insertAlam(alarm: Alarm): Alarm {
        return withContext(Dispatchers.IO) {
            alarmQueries.insert(alarm.id, alarm.hour, alarm.minute, alarm.enabled, alarm.sound).value
            val resolvedId = alarm.id ?: alarmQueries.lastInsertRowId().executeAsOne().toInt()
            alarm.copy(id = resolvedId)
        }
    }

    suspend fun deleteAlarm(alarm: Alarm) {
        withContext(Dispatchers.IO) {
            alarm.id?.let { alarmQueries.delete(it) }
        }
    }
}

internal fun AlarmDbo.toAlarm() =
    Alarm(hour, minute, RepeatMode.OneTime, id, snooze = null, enabled, sound = sound)