package io.github.ppking2897.travelrecord.core.data.local.db

import android.content.ContextWrapper
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.RoomItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.RoomItineraryItemRepository
import io.github.ppking2897.travelrecord.test.TestRepositories
import kotlinx.coroutines.Dispatchers

/**
 * 開在記憶體中的 App 資料庫
 *
 * JVM 測試沒有真的 Android Context：記憶體資料庫用不到 Context 的功能，
 * 傳空的 ContextWrapper 即可（需要 withHostTest 開啟 isReturnDefaultValues）。
 */
fun inMemoryAppDatabase(): AppDatabase =
    Room.inMemoryDatabaseBuilder<AppDatabase>(ContextWrapper(null))
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

fun AppDatabase.repositories() = TestRepositories(
    itineraries = RoomItineraryRepository(this),
    items = RoomItineraryItemRepository(this),
)
