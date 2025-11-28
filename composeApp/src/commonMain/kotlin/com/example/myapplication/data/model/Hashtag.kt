package com.example.myapplication.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 標籤資料模型
 * 
 * @property tag 標籤文字（不含 #）
 * @property usageCount 使用次數
 * @property firstUsed 首次使用時間
 * @property lastUsed 最後使用時間
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Serializable
data class Hashtag(
    val tag: String,
    val usageCount: Int,
    @Contextual val firstUsed: Instant,
    @Contextual val lastUsed: Instant
)
