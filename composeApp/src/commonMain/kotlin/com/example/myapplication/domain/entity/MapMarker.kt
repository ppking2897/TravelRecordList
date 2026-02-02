package com.example.myapplication.domain.entity

import kotlinx.datetime.LocalDate

/**
 * 地圖標記模型
 *
 * 用於在地圖上顯示行程項目的位置標記
 *
 * @property id 唯一識別碼（對應 ItineraryItem.id）
 * @property latitude 緯度
 * @property longitude 經度
 * @property title 標記標題（活動名稱）
 * @property locationName 地點名稱
 * @property date 日期
 * @property order 當日順序（從 1 開始）
 * @property isCompleted 是否已完成
 */
data class MapMarker(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val locationName: String,
    val date: LocalDate,
    val order: Int,
    val isCompleted: Boolean
)
