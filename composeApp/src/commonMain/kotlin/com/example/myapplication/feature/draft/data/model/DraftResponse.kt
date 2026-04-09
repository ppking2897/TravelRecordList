@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.feature.draft.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 草稿類型
 */
@Serializable
enum class DraftTypeResponse {
    ITINERARY,
    ITEM
}

/**
 * 草稿資料傳輸物件
 */
@Serializable
data class DraftResponse(
    val id: String,
    val type: DraftTypeResponse,
    val data: Map<String, String>,
    @Contextual val createdAt: Instant,
    @Contextual val modifiedAt: Instant
)
