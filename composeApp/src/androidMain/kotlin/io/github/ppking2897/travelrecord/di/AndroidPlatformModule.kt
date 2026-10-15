package io.github.ppking2897.travelrecord.di

import io.github.ppking2897.travelrecord.core.data.local.AndroidImageStorageService
import io.github.ppking2897.travelrecord.core.data.local.AndroidStorageService
import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.core.data.local.StorageService
import io.github.ppking2897.travelrecord.core.data.local.db.createAppDatabase
import io.github.ppking2897.travelrecord.core.data.remote.AndroidAppIdentity
import io.github.ppking2897.travelrecord.core.data.remote.AppIdentity
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module

actual fun Module.registerPlatformStorageService() {
    single<StorageService> { AndroidStorageService(androidContext()) }
}

actual fun Module.registerPlatformImageStorageService() {
    single<ImageStorageService> { AndroidImageStorageService(androidContext()) }
}

actual fun Module.registerPlatformAppIdentity() {
    single<AppIdentity> { AndroidAppIdentity(androidContext()) }
}

actual fun Module.registerPlatformDatabase() {
    single { createAppDatabase(androidContext()) }
}
