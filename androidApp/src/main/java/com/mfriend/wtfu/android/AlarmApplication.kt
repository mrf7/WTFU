package com.mfriend.wtfu.android

import android.app.Application
import android.content.Context
import com.mfriend.wtfu.di.initKoin
import com.mfriend.wtfu.models.AlarmViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

class AlarmApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            module {
                viewModelOf(::AlarmViewModel)
                single<Context> { this@AlarmApplication }
            }
        )
    }
}