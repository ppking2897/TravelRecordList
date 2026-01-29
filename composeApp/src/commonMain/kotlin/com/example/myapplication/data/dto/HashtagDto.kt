package com.example.myapplication.data.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 標籤資料傳輸物件
 */
@Serializable
data class HashtagDto(
    val tag: String,
    val usageCount: Int,
    @Contextual val firstUsed: Instant,
    @Contextual val lastUsed: Instant
)
