package io.github.ppking2897.travelrecord.di

import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.core.data.local.StorageService
import io.github.ppking2897.travelrecord.core.data.remote.AppIdentity
import io.github.ppking2897.travelrecord.data.storage.IosStorageService
import org.koin.core.module.Module

actual fun Module.registerPlatformStorageService() {
    single<StorageService> { IosStorageService() }
}

actual fun Module.registerPlatformImageStorageService() {
    single<ImageStorageService> {
        object : ImageStorageService {
            override suspend fun saveImage(imageData: ByteArray, itemId: String): Result<String> = Result.success("dummy_path")
            override suspend fun loadImage(imagePath: String): Result<ByteArray> = Result.success(ByteArray(0))
            override suspend fun deleteImage(imagePath: String): Result<Unit> = Result.success(Unit)
            override suspend fun compressImage(imageData: ByteArray, maxSizeKB: Int): Result<ByteArray> = Result.success(imageData)
            override suspend fun generateThumbnail(imageData: ByteArray, width: Int, height: Int): Result<ByteArray> = Result.success(imageData)
            override fun getImageDirectory(): String = ""
        }
    }
}

actual fun Module.registerPlatformAppIdentity() {
    // iOS 尚未讀取 API key（見 ApiKeyProvider.ios.kt 的等待標記），先不附身分 header
    single<AppIdentity> {
        object : AppIdentity {
            override val requestHeaders: Map<String, String> = emptyMap()
        }
    }
}

/** 提供給 iOS Swift 呼叫的 Koin 初始化函數 */
fun initKoinIos() = initKoin()
