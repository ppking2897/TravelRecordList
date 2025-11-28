package com.example.myapplication.ui.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android 平台的圖片選擇器實作
 * 
 * 注意：此實作需要在 Activity 中註冊 ActivityResultLauncher
 * 實際使用時可能需要調整為使用 Compose 的 rememberLauncherForActivityResult
 */
class AndroidImagePicker(
    private val activity: Activity
) : ImagePicker {
    
    override suspend fun pickSingleImage(): ByteArray? = suspendCancellableCoroutine { continuation ->
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        
        // 這裡需要使用 ActivityResultLauncher
        // 實際實作時應該在 Activity/Fragment 中註冊
        // 這裡提供基本架構
        
        // TODO: 實作實際的圖片選擇邏輯
        // 暫時返回 null
        continuation.resume(null)
    }
    
    override suspend fun pickMultipleImages(maxCount: Int): List<ByteArray> = suspendCancellableCoroutine { continuation ->
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        
        // TODO: 實作實際的多張圖片選擇邏輯
        // 暫時返回空列表
        continuation.resume(emptyList())
    }
    
    /**
     * 從 Uri 讀取圖片資料
     */
    private fun readImageData(uri: Uri): ByteArray? {
        return try {
            activity.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            null
        }
    }
}
