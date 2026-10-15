package io.github.ppking2897.travelrecord.feature.itinerary.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 行程資料表
 *
 * 欄位只用基本型別，與領域模型的轉換在 ItineraryEntityMapper：
 * 日期存 epoch 天數、時間點存 epoch 毫秒。行程的項目另存在 itinerary_items 表。
 */
@Entity(tableName = "itineraries")
data class ItineraryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val startDate: Long?,
    val endDate: Long?,
    val coverPhotoPath: String?,
    val createdAt: Long,
    val modifiedAt: Long,
)
