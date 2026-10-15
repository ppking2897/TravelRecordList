package io.github.ppking2897.travelrecord.feature.itinerary_item.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.ppking2897.travelrecord.feature.itinerary.data.local.ItineraryEntity

/**
 * 行程項目資料表，每個項目只存這一份
 *
 * 刪除行程時連帶刪除它的項目。照片與標籤以 JSON 字串存在同一列，保留清單順序；
 * 其餘轉換規則見 ItineraryItemEntityMapper。
 */
@Entity(
    tableName = "itinerary_items",
    foreignKeys = [
        ForeignKey(
            entity = ItineraryEntity::class,
            parentColumns = ["id"],
            childColumns = ["itineraryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("itineraryId")],
)
data class ItineraryItemEntity(
    @PrimaryKey val id: String,
    val itineraryId: String,
    val date: Long,
    val arrivalTime: Long?,
    val departureTime: Long?,
    val locationName: String,
    val locationLatitude: Double?,
    val locationLongitude: Double?,
    val locationAddress: String?,
    val activity: String,
    val notes: String,
    val hashtagsJson: String,
    val photosJson: String,
    val coverPhotoId: String?,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val sortOrder: Int,
    val createdAt: Long,
    val modifiedAt: Long,
)
