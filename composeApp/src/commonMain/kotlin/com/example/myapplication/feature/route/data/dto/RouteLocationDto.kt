package com.example.myapplication.feature.route.data.dto

import com.example.myapplication.feature.itinerary_item.data.dto.LocationDto
import kotlin.time.Duration
import kotlinx.serialization.Serializable

/**
 * 路線地點資料傳輸物件
 */
@Serializable
data class RouteLocationDto(
    val location: LocationDto,
    val order: Int,
    val recommendedDuration: Duration? = null,
    val notes: String = ""
)
