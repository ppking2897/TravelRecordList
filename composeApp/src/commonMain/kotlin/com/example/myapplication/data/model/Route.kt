package com.example.myapplication.data.model

import kotlin.time.Duration
import kotlinx.serialization.Serializable

/**
 * 可分享的旅遊路線資料模型
 * 
 * @property id 唯一識別碼
 * @property title 標題
 * @property locations 路線地點列表
 * @property createdFrom 來源行程 ID
 */
@Serializable
data class Route(
    val id: String,
    val title: String,
    val locations: List<RouteLocation>,
    val createdFrom: String
)

/**
 * 路線地點資料模型
 * 
 * @property location 地點資訊
 * @property order 順序
 * @property recommendedDuration 建議停留時間（可選）
 * @property notes 備註
 */
@Serializable
data class RouteLocation(
    val location: Location,
    val order: Int,
    val recommendedDuration: Duration? = null,
    val notes: String = ""
)
