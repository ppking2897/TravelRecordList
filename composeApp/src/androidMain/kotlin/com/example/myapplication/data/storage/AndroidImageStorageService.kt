package com.example.myapplication.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Android 平台的圖片儲存服務實作
 */
class AndroidImageStorageService(
    private val context: Context
) : ImageStorageService {
    
    private val imageDir: File by lazy {
        File(context.filesDir, "itinerary_images").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    override suspend fun saveImage(imageData: ByteArray, itemId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 檢查儲存空間
            val availableSpace = imageDir.usableSpace
            if (availableSpace < imageData.size * 2) {
                return@withContext Result.failure(
                    ImageStorageError.InsufficientStorage("儲存空間不足")
                )
            }
            
            // 生成唯一檔名
            val fileName = "${itemId}_${UUID.randomUUID()}.jpg"
            val file = File(imageDir, fileName)
            
            // 壓縮並儲存圖片
            val compressedData = compressImage(imageData).getOrThrow()
            FileOutputStream(file).use { output ->
                output.write(compressedData)
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(ImageStorageError.SaveFailed("儲存圖片失敗: ${e.message}"))
        }
    }
    
    override suspend fun loadImage(imagePath: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (!file.exists()) {
                return@withContext Result.failure(
                    ImageStorageError.LoadFailed("圖片檔案不存在")
                )
            }
            
            val data = file.readBytes()
            Result.success(data)
        } catch (e: Exception) {
            Result.failure(ImageStorageError.LoadFailed("讀取圖片失敗: ${e.message}"))
        }
    }
    
    override suspend fun deleteImage(imagePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(ImageStorageError.DeleteFailed("刪除圖片失敗: ${e.message}"))
        }
    }
    
    override suspend fun compressImage(imageData: ByteArray, maxSizeKB: Int): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                ?: return@withContext Result.failure(
                    ImageStorageError.CompressionFailed("無法解碼圖片")
                )
            
            var quality = 90
            var compressedData: ByteArray
            
            do {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                compressedData = outputStream.toByteArray()
                quality -= 10
            } while (compressedData.size > maxSizeKB * 1024 && quality > 10)
            
            bitmap.recycle()
            Result.success(compressedData)
        } catch (e: Exception) {
            Result.failure(ImageStorageError.CompressionFailed("壓縮圖片失敗: ${e.message}"))
        }
    }
    
    override suspend fun generateThumbnail(
        imageData: ByteArray,
        width: Int,
        height: Int
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                ?: return@withContext Result.failure(
                    ImageStorageError.CompressionFailed("無法解碼圖片")
                )
            
            // 計算縮放比例
            val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val targetWidth: Int
            val targetHeight: Int
            
            if (aspectRatio > 1) {
                targetWidth = width
                targetHeight = (width / aspectRatio).toInt()
            } else {
                targetHeight = height
                targetWidth = (height * aspectRatio).toInt()
            }
            
            // 建立縮圖
            val thumbnail = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            
            // 轉換為 ByteArray
            val outputStream = ByteArrayOutputStream()
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val thumbnailData = outputStream.toByteArray()
            
            bitmap.recycle()
            thumbnail.recycle()
            
            Result.success(thumbnailData)
        } catch (e: Exception) {
            Result.failure(ImageStorageError.CompressionFailed("生成縮圖失敗: ${e.message}"))
        }
    }
    
    override fun getImageDirectory(): String = imageDir.absolutePath
}
