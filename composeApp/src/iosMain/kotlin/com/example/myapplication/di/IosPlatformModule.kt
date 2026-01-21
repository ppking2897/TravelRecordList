package com.example.myapplication.di

import com.example.myapplication.data.storage.ImageStorageService
import com.example.myapplication.data.storage.IosStorageService
import com.example.myapplication.data.storage.StorageService
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS 平台特定的依賴注入模組
 */
val iosPlatformModule = module {
    single<StorageService> { IosStorageService() }
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

actual val platformModule: Module = iosPlatformModule

/** 提供給 iOS Swift 呼叫的 Koin 初始化函數 */
fun initKoinIos() = initKoin()
