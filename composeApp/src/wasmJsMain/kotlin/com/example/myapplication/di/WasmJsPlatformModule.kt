package com.example.myapplication.di

import com.example.myapplication.data.storage.LocalStorageStorageService
import com.example.myapplication.data.storage.StorageService
import com.example.myapplication.util.ImageSaver
import org.koin.dsl.module

/**
 * WasmJS 平台特定的依賴注入模組
 */
val wasmJsPlatformModule = module {
    single<StorageService> { LocalStorageStorageService() }
    single { ImageSaver() }
}

actual val platformModule: Module = wasmJsPlatformModule
