@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.itinerary_item.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 標籤資料傳輸物件
 */
@Serializable
data class HashtagResponse(
    val tag: String,
    val usageCount: Int,
    @Contextual val firstUsed: Instant,
    @Contextual val lastUsed: Instant
)
