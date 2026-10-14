package com.example.myapplication.di

import com.example.myapplication.core.data.local.AndroidImageStorageService
import com.example.myapplication.core.data.local.AndroidStorageService
import com.example.myapplication.core.domain.service.ImageStorageService
import com.example.myapplication.core.data.local.StorageService
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module

actual fun Module.registerPlatformStorageService() {
    single<StorageService> { AndroidStorageService(androidContext()) }
}

actual fun Module.registerPlatformImageStorageService() {
    single<ImageStorageService> { AndroidImageStorageService(androidContext()) }
}
