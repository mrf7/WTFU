package com.mfriend.wtfu

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import com.mfriend.wtfu.di.initKoin
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class AlarmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            module {
                viewModelOf(::AlarmViewModel)
                viewModelOf(::AlarmTriggerViewModel)
                single<Context> { this@AlarmApplication }
                single<AlarmManager> { get<Context>().getSystemService(ALARM_SERVICE) as AlarmManager }
                single<AlarmScheduler> { AndroidAlarmScheduler(get(), get()) }
                single<AlarmRinger> { AndroidAlarmRinger(get()) }
                single { get<Context>().getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
            },
        ) {
            androidContext(this@AlarmApplication)
        }
        Thread {
            try {
                runBlocking {
                    val database = get<DatabaseHelper>()
                    val scheduler = get<AlarmScheduler>()
                    database.getEnabledAlarms().forEach { scheduler.schedule(it) }
                }
            } catch (e: Exception) {
                android.util.Log.e("AlarmApplication", "Failed to restore alarms on startup", e)
            }
        }.start()
    }
}
