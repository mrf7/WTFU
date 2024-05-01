package com.mfriend.wtfu

interface AlarmScheduler {
    /**
     * Schedules [alarm] to go off via launching the app at the given time
     */
    fun scheduleLaunch(alarm: Alarm)

    /**
     * Schedules [alarm] to go off via a (full screen) notification
     */
    fun scheduleNotification(alarm: Alarm)
}