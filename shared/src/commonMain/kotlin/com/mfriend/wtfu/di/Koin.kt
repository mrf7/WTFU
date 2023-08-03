package com.mfriend.wtfu.di

import com.mfriend.wtfu.DatabaseHelper
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun initKoin(appModule: Module): KoinApplication = startKoin { modules(appModule, platformModule, coreModule) }

private val coreModule = module {
    singleOf(::DatabaseHelper)
}

internal expect val platformModule: Module