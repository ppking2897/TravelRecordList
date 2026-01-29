@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.domain.entity

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Duration

/**
 * 行程項目領域模型
 *
 * @property id 唯一識別碼
 * @property itineraryId 所屬行程的 ID
 * @property date 日期
 * @property arrivalTime 到達時間（可選）
 * @property departureTime 離開時間（可選）
 * @property location 地點
 * @property activity 活動描述
 * @property notes 備註
 * @property hashtags 標籤列表
 * @property photos 照片列表
 * @property coverPhotoId 封面照片 ID
 * @property isCompleted 是否已完成
 * @property completedAt 完成時間戳記（只有當 isCompleted 為 true 時才設定）
 * @property createdAt 建立時間戳記
 * @property modifiedAt 修改時間戳記
 */
data class ItineraryItem(
    val id: String,
    val itineraryId: String,
    val date: LocalDate,
    val arrivalTime: LocalTime? = null,
    val departureTime: LocalTime? = null,
    val location: Location,
    val activity: String,
    val notes: String = "",
    val hashtags: List<Hashtag> = emptyList(),
    val photos: List<Photo> = emptyList(),
    val coverPhotoId: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: Instant? = null,
    val createdAt: Instant,
    val modifiedAt: Instant
) {
    /**
     * 計算停留時間
     */
    fun stayDuration(): Duration? {
        return if (arrivalTime != null && departureTime != null) {
            val arrivalSeconds = arrivalTime.toSecondOfDay()
            val departureSeconds = departureTime.toSecondOfDay()
            Duration.parse("PT${departureSeconds - arrivalSeconds}S")
        } else {
            null
        }
    }

    /**
     * 取得主要時間（用於排序）
     */
    fun primaryTime(): LocalTime? = arrivalTime ?: departureTime

    /**
     * 檢查是否有完整的時間資訊
     */
    fun hasCompleteTimeInfo(): Boolean = arrivalTime != null && departureTime != null

    /**
     * 取得封面照片
     */
    fun getCoverPhoto(): Photo? = photos.find { it.id == coverPhotoId }
}
