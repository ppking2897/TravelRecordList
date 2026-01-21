package com.example.myapplication.data.storage

/**
 * 圖片儲存服務介面
 * 負責處理圖片的儲存、讀取、壓縮和縮圖生成
 */
interface ImageStorageService {
    /**
     * 儲存圖片到本地儲存
     * @param imageData 原始圖片資料
     * @param itemId 所屬的行程項目 ID
     * @return 儲存後的圖片檔案路徑
     */
    suspend fun saveImage(imageData: ByteArray, itemId: String): Result<String>
    
    /**
     * 讀取圖片資料
     * @param imagePath 圖片檔案路徑
     * @return 圖片資料
     */
    suspend fun loadImage(imagePath: String): Result<ByteArray>
    
    /**
     * 刪除圖片
     * @param imagePath 圖片檔案路徑
     */
    suspend fun deleteImage(imagePath: String): Result<Unit>
    
    /**
     * 壓縮圖片
     * @param imageData 原始圖片資料
     * @param maxSizeKB 最大檔案大小（KB）
     * @return 壓縮後的圖片資料
     */
    suspend fun compressImage(imageData: ByteArray, maxSizeKB: Int = 500): Result<ByteArray>
    
    /**
     * 生成縮圖
     * @param imageData 原始圖片資料
     * @param width 縮圖寬度
     * @param height 縮圖高度
     * @return 縮圖資料
     */
    suspend fun generateThumbnail(
        imageData: ByteArray,
        width: Int = 200,
        height: Int = 200
    ): Result<ByteArray>
    
    /**
     * 取得圖片儲存目錄
     */
    fun getImageDirectory(): String
}

/**
 * 圖片儲存錯誤
 */
sealed class ImageStorageError : Exception() {
    data class SaveFailed(override val message: String) : ImageStorageError()
    data class LoadFailed(override val message: String) : ImageStorageError()
    data class DeleteFailed(override val message: String) : ImageStorageError()
    data class CompressionFailed(override val message: String) : ImageStorageError()
    data class InsufficientStorage(override val message: String) : ImageStorageError()
}
