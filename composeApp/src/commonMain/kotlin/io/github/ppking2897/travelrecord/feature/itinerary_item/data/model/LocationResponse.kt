package com.example.myapplication.feature.itinerary_item.data.model

import kotlinx.serialization.Serializable

/**
 * 地點資料傳輸物件
 */
@Serializable
data class LocationResponse(
    val name: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null
)
