package com.example.myapplication.di

import com.example.myapplication.data.storage.AndroidImageStorageService
import com.example.myapplication.data.storage.AndroidStorageService
import com.example.myapplication.data.storage.ImageStorageService
import com.example.myapplication.data.storage.StorageService
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
