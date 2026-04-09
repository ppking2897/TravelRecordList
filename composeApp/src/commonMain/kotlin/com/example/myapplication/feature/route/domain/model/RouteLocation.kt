package com.example.myapplication.feature.route.domain.model

import com.example.myapplication.feature.itinerary_item.domain.model.Location
import kotlin.time.Duration

/**
 * 路線地點領域模型
 *
 * @property location 地點資訊
 * @property order 順序
 * @property recommendedDuration 建議停留時間（可選）
 * @property notes 備註
 */
data class RouteLocation(
    val location: Location,
    val order: Int,
    val recommendedDuration: Duration? = null,
    val notes: String = ""
)
