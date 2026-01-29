package com.example.myapplication.domain.entity

import kotlinx.datetime.Instant

/**
 * 照片領域模型
 *
 * @property id 唯一識別碼
 * @property itemId 所屬行程項目的 ID
 * @property fileName 檔案名稱
 * @property filePath 本地儲存路徑
 * @property thumbnailPath 縮圖路徑（可選）
 * @property order 顯示順序
 * @property isCover 是否為封面照片
 * @property width 原始寬度（可選）
 * @property height 原始高度（可選）
 * @property fileSize 檔案大小（bytes）
 * @property uploadedAt 上傳時間戳記
 * @property modifiedAt 修改時間戳記
 */
data class Photo(
    val id: String,
    val itemId: String,
    val fileName: String,
    val filePath: String,
    val thumbnailPath: String? = null,
    val order: Int,
    val isCover: Boolean = false,
    val width: Int? = null,
    val height: Int? = null,
    val fileSize: Long,
    val uploadedAt: Instant,
    val modifiedAt: Instant
)
