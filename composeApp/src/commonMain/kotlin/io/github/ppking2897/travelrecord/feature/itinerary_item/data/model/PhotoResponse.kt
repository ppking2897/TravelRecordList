@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.itinerary_item.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 照片資料傳輸物件
 */
@Serializable
data class PhotoResponse(
    val id: String,
    val itemId: String,
    val fileName: String,
    val filePath: String,
    val thumbnailPath: String? = null,
    val order: Int,
    val isCover: Boolean = false,
    val width: Int? = null,
    val height: Int? = null,
    val fileSize: Long,
    @Contextual val uploadedAt: Instant,
    @Contextual val modifiedAt: Instant
)
