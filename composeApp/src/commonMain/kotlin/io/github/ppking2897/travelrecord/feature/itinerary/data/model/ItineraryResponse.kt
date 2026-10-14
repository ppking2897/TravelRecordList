@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.feature.itinerary.data.model

import com.example.myapplication.feature.itinerary_item.data.model.ItineraryItemResponse
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 旅遊行程資料傳輸物件
 */
@Serializable
data class ItineraryResponse(
    val id: String,
    val title: String,
    val description: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val coverPhotoPath: String? = null,
    val items: List<ItineraryItemResponse> = emptyList(),
    @Contextual val createdAt: Instant,
    @Contextual val modifiedAt: Instant
)
