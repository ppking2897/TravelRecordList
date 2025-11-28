package com.example.myapplication.ui.util

/**
 * 圖片選擇器介面
 * 用於從裝置選擇照片
 */
interface ImagePicker {
    /**
     * 選擇單張照片
     * @return 照片資料，如果取消則返回 null
     */
    suspend fun pickSingleImage(): ByteArray?
    
    /**
     * 選擇多張照片
     * @param maxCount 最大選擇數量
     * @return 照片資料列表
     */
    suspend fun pickMultipleImages(maxCount: Int = 10): List<ByteArray>
}
