package com.mfriend.wtfu

interface AlarmScheduler {
    /** Schedule the next occurrence; no-op if disabled or no computable next time. */
    fun schedule(alarm: Alarm)

    /** Cancel any pending alarm for this id. */
    fun cancel(alarmId: Int)
}
