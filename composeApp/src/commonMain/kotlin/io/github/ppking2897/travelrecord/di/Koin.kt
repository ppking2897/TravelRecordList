package io.github.ppking2897.travelrecord.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

/**
 * 各平台必須實作的服務註冊（漏實作則編譯失敗）
 */
expect fun Module.registerPlatformStorageService()
expect fun Module.registerPlatformImageStorageService()

/**
 * 平台模組（commonMain 定義，各平台透過 expect fun 提供實作）
 */
val platformModule = module {
    registerPlatformStorageService()
    registerPlatformImageStorageService()
}

/**
 * 初始化 Koin 依賴注入
 */
@OptIn(ExperimentalTime::class)
fun initKoin(
    appDeclaration: KoinAppDeclaration = {}
) = startKoin {
    appDeclaration()
    modules(platformModule)
    modules(appModule)
}
