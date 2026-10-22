package io.github.ppking2897.travelrecord.feature.route.domain.model

import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location

/**
 * 路線地點領域模型
 *
 * @property location 地點資訊
 * @property order 順序
 * @property notes 備註
 */
data class RouteLocation(
    val location: Location,
    val order: Int,
    val notes: String = ""
)
