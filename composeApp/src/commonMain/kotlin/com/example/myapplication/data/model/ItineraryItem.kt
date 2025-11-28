package com.example.myapplication.data.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 行程項目資料模型
 * 
 * @property id 唯一識別碼
 * @property itineraryId 所屬行程的 ID
 * @property date 日期
 * @property arrivalTime 到達時間（可選）
 * @property departureTime 離開時間（可選）
 * @property location 地點
 * @property activity 活動描述
 * @property notes 備註
 * @property isCompleted 是否已完成
 * @property completedAt 完成時間戳記（只有當 isCompleted 為 true 時才設定）
 * @property photoReferences 照片參考列表
 * @property createdAt 建立時間戳記
 * @property modifiedAt 修改時間戳記
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Serializable
data class ItineraryItem(
    val id: String,
    val itineraryId: String,
    val date: LocalDate,
    val arrivalTime: LocalTime? = null,      // 到達時間（可選）
    val departureTime: LocalTime? = null,    // 離開時間（可選）
    val location: Location,
    val activity: String,
    val notes: String = "",
    val hashtags: List<Hashtag> = emptyList(),  // 新增：標籤列表
    val photos: List<Photo> = emptyList(),     // 新增：照片列表
    val coverPhotoId: String? = null,          // 新增：封面照片 ID
    val isCompleted: Boolean = false,
    @Contextual val completedAt: Instant? = null,
    val photoReferences: List<String> = emptyList(),  // 保留向後相容
    @Contextual val createdAt: Instant,
    @Contextual val modifiedAt: Instant
) {
    /**
     * 計算停留時間
     * 如果有到達和離開時間，計算兩者之間的時間差
     */
    fun stayDuration(): kotlin.time.Duration? {
        return if (arrivalTime != null && departureTime != null) {
            val arrivalSeconds = arrivalTime.toSecondOfDay()
            val departureSeconds = departureTime.toSecondOfDay()
            kotlin.time.Duration.Companion.parse("PT${departureSeconds - arrivalSeconds}S")
        } else {
            null
        }
    }
    
    /**
     * 取得主要時間（用於排序）
     * 優先使用到達時間，如果沒有則使用離開時間
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
