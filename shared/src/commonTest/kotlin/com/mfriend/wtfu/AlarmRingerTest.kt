package com.mfriend.wtfu

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class AlarmRingerTest : FunSpec({
    test("stop invokes AlarmRinger.stop with alarm id") {
        val ringer = FakeAlarmRinger()
        ringer.stop(42)
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
