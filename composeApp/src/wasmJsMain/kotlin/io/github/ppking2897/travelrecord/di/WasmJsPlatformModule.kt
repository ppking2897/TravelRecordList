package com.example.myapplication.di

import com.example.myapplication.core.data.local.StorageService
import com.example.myapplication.core.domain.service.ImageStorageService
import com.example.myapplication.data.storage.LocalStorageStorageService
import org.koin.core.module.Module

actual fun Module.registerPlatformStorageService() {
    single<StorageService> { LocalStorageStorageService() }
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
