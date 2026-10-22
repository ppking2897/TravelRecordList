package io.github.ppking2897.travelrecord.feature.route.data.model

import io.github.ppking2897.travelrecord.feature.itinerary_item.data.model.LocationResponse
import kotlinx.serialization.Serializable

/**
 * 路線地點資料傳輸物件
 */
@Serializable
data class RouteLocationResponse(
    val location: LocationResponse,
    val order: Int,
    val notes: String = ""
)
