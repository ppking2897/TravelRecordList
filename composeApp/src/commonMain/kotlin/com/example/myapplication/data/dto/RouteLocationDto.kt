package com.example.myapplication.data.dto

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
