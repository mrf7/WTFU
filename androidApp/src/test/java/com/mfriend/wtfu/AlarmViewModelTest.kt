package com.mfriend.wtfu

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.mfriend.AlarmDb
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class AlarmViewModelTest : FunSpec({
    test("stopRinging delegates to AlarmRinger.stop") {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { AlarmDb.Schema.create(it) }
        val ringer = FakeAlarmRinger()
        val viewModel = AlarmViewModel(DatabaseHelper(driver), FakeAlarmScheduler(), ringer)

        viewModel.stopRinging(42)

        ringer.stoppedIds shouldContainExactly listOf(42)
    }
})

private class FakeAlarmRinger : AlarmRinger {
    val stoppedIds = mutableListOf<Int>()

    override fun start(alarmId: Int) = Unit

    override fun stop(alarmId: Int) {
        stoppedIds.add(alarmId)
    }
}

private class FakeAlarmScheduler : AlarmScheduler {
    override fun schedule(alarm: Alarm) = Unit

    override fun cancel(alarmId: Int) = Unit
}
