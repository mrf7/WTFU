package com.mfriend.wtfu.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.mfriend.AlarmDb
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single<SqlDriver> { AndroidSqliteDriver(AlarmDb.Schema, get(), "AlarmDb") }
}