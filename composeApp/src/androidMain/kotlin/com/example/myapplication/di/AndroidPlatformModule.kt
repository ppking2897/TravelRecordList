package com.example.myapplication.di

import com.example.myapplication.core.data.local.AndroidImageStorageService
import com.example.myapplication.core.data.local.AndroidStorageService
import com.example.myapplication.core.domain.service.ImageStorageService
import com.example.myapplication.core.data.local.StorageService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android 平台特定的依賴注入模組
 */
val androidPlatformModule = module {
    single<StorageService> { AndroidStorageService(androidContext()) }
    single<ImageStorageService> { AndroidImageStorageService(androidContext()) }
}

actual val platformModule: Module = androidPlatformModule
