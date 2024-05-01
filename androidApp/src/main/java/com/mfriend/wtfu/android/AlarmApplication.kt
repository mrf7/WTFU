package com.mfriend.wtfu.android

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.content.Context
import com.mfriend.wtfu.AlarmScheduler
import com.mfriend.wtfu.di.initKoin
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

class AlarmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            module {
                viewModelOf(::AlarmViewModel)
                single<Context> { this@AlarmApplication }
                single<AlarmManager> { get<Context>().getSystemService(Context.ALARM_SERVICE) as AlarmManager }
                single<AlarmScheduler> { AndroidAlarmScheduler(get(), get()) }
                single { get<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
            }
        )
    }
}