package com.example.myapplication.data.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 行程項目資料模型
 * 
 * @property id 唯一識別碼
 * @property itineraryId 所屬行程的 ID
 * @property date 日期
 * @property time 時間（可選）
 * @property location 地點
 * @property activity 活動描述
 * @property notes 備註
 * @property isCompleted 是否已完成
 * @property completedAt 完成時間戳記（只有當 isCompleted 為 true 時才設定）
 * @property photoReferences 照片參考列表
 * @property createdAt 建立時間戳記
 * @property modifiedAt 修改時間戳記
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Serializable
data class ItineraryItem(
    val id: String,
    val itineraryId: String,
    val date: LocalDate,
    val time: LocalTime? = null,
    val location: Location,
    val activity: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    @Contextual val completedAt: Instant? = null,
    val photoReferences: List<String> = emptyList(),
    @Contextual val createdAt: Instant,
    @Contextual val modifiedAt: Instant
)
