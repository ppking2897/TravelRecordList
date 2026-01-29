@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.domain.entity

import kotlinx.datetime.Instant

/**
 * 草稿領域模型
 *
 * 用於暫存未完成的行程或項目資料
 *
 * @property id 唯一識別碼
 * @property type 草稿類型（行程或項目）
 * @property data 草稿資料（以 Map 形式儲存）
 * @property createdAt 建立時間戳記
 * @property modifiedAt 修改時間戳記
 */
data class Draft(
    val id: String,
    val type: DraftType,
    val data: Map<String, String>,
    val createdAt: Instant,
    val modifiedAt: Instant
)
