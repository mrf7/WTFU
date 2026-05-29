package com.mfriend.wtfu.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.mfriend.AlarmDb
import com.mfriend.wtfu.AlarmRinger
import com.mfriend.wtfu.AlarmScheduler
import com.mfriend.wtfu.IosAlarmRinger
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single<SqlDriver> { NativeSqliteDriver(AlarmDb.Schema, "AlarmDb") }
    single<AlarmScheduler> { IosAlarmScheduler() }
    single<AlarmRinger> { IosAlarmRinger() }
}