package io.github.ppking2897.travelrecord.core.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import io.github.ppking2897.travelrecord.feature.itinerary.data.local.ItineraryDao
import io.github.ppking2897.travelrecord.feature.itinerary.data.local.ItineraryEntity
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.local.ItineraryItemDao
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.local.ItineraryItemEntity

/**
 * App 的本機資料庫
 *
 * 每次改動資料表都要升版並提供 Migration；匯出的 schema 在 composeApp/schemas/，是資料版本的紀錄。
 */
@Database(
    entities = [ItineraryEntity::class, ItineraryItemEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itineraryDao(): ItineraryDao
    abstract fun itineraryItemDao(): ItineraryItemDao
}

/** 實作由 Room 的 KSP 依各平台產生 */
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

const val APP_DATABASE_NAME = "travel_record.db"
