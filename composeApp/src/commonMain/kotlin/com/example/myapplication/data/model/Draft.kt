package com.example.myapplication.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 草稿資料模型
 * 
 * 用於暫存未完成的行程或項目資料
 * 
 * @property id 唯一識別碼
 * @property type 草稿類型（行程或項目）
 * @property data 草稿資料（以 Map 形式儲存）
 * @property createdAt 建立時間戳記
 * @property modifiedAt 修改時間戳記
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Serializable
data class Draft(
    val id: String,
    val type: DraftType,
    val data: Map<String, String>,
    @Contextual val createdAt: Instant,
    @Contextual val modifiedAt: Instant
)

/**
 * 草稿類型
 */
@Serializable
enum class DraftType {
    /** 行程草稿 */
    ITINERARY,
    
    /** 項目草稿 */
    ITEM
}
