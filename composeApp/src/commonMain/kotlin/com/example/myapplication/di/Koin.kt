package com.example.myapplication.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import kotlin.time.ExperimentalTime

/**
 * 平台特定的模組
 */
expect val platformModule: Module

/**
 * 初始化 Koin 依賴注入
 * 
 * @param appDeclaration Koin 應用程式宣告
 */
@OptIn(ExperimentalTime::class)
fun initKoin(
    appDeclaration: KoinAppDeclaration = {}
) = startKoin {
    appDeclaration()
    modules(platformModule, appModule)
}
