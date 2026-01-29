@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 草稿類型
 */
@Serializable
enum class DraftTypeDto {
    ITINERARY,
    ITEM
}

/**
 * 草稿資料傳輸物件
 */
@Serializable
data class DraftDto(
    val id: String,
    val type: DraftTypeDto,
    val data: Map<String, String>,
    @Contextual val createdAt: Instant,
    @Contextual val modifiedAt: Instant
)
