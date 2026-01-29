package com.example.myapplication.domain.entity

import kotlinx.datetime.Instant

/**
 * 標籤領域模型
 *
 * @property tag 標籤文字（不含 #）
 * @property usageCount 使用次數
 * @property firstUsed 首次使用時間
 * @property lastUsed 最後使用時間
 */
data class Hashtag(
    val tag: String,
    val usageCount: Int,
    val firstUsed: Instant,
    val lastUsed: Instant
)
