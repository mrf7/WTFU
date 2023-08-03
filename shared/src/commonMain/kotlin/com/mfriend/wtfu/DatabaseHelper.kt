package com.mfriend.wtfu

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.mfriend.AlarmDb
import com.mfriend.AlarmDbo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DatabaseHelper(driver: SqlDriver) {
    private val database = AlarmDb(driver)
    private val alarmQueries = database.alarmQueries
    fun getAlarms(): Flow<List<AlarmDbo>> {
        return alarmQueries.selectAll().asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun insertAlam(alarmDbo: AlarmDbo) {
        withContext(Dispatchers.IO) {
            alarmQueries.insertAlarmDbo(alarmDbo)
        }
    }
}