package com.example.myapplication.data.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 旅遊行程資料模型
 * 
 * @property id 唯一識別碼
 * @property title 標題
 * @property description 描述
 * @property startDate 開始日期（可選）
 * @property endDate 結束日期（可選）
 * @property items 行程項目列表
 * @property createdAt 建立時間戳記
 * @property modifiedAt 修改時間戳記
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Serializable
data class Itinerary(
    val id: String,
    val title: String,
    val description: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val items: List<ItineraryItem> = emptyList(),
    @Contextual val createdAt: Instant,
    @Contextual val modifiedAt: Instant
)
