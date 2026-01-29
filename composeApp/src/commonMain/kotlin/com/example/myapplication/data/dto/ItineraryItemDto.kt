@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.data.dto

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 行程項目資料傳輸物件
 */
@Serializable
data class ItineraryItemDto(
    val id: String,
    val itineraryId: String,
    val date: LocalDate,
    val arrivalTime: LocalTime? = null,
    val departureTime: LocalTime? = null,
    val location: LocationDto,
    val activity: String,
    val notes: String = "",
    val hashtags: List<HashtagDto> = emptyList(),
    val photos: List<PhotoDto> = emptyList(),
    val coverPhotoId: String? = null,
    val isCompleted: Boolean = false,
    @Contextual val completedAt: Instant? = null,
    @Contextual val createdAt: Instant,
    @Contextual val modifiedAt: Instant
)
