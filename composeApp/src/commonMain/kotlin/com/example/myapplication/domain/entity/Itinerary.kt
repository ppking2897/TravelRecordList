@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.domain.entity

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * 旅遊行程領域模型
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
data class Itinerary(
    val id: String,
    val title: String,
    val description: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val items: List<ItineraryItem> = emptyList(),
    val createdAt: Instant,
    val modifiedAt: Instant
)
