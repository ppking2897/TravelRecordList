package io.github.ppking2897.travelrecord.core.data.local.db

import android.content.ContextWrapper
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.ppking2897.travelrecord.core.data.DataLayerAcceptanceContract
import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.ItineraryRepositoryContract
import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.RoomItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.ItineraryItemRepositoryContract
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.RoomItineraryItemRepository
import io.github.ppking2897.travelrecord.test.TestRepositories
import kotlinx.coroutines.Dispatchers

/**
 * 以 Room 實作的 Repository，資料庫開在記憶體中
 *
 * JVM 測試沒有真的 Android Context：記憶體資料庫用不到 Context 的功能，
 * 傳空的 ContextWrapper 即可（需要 withHostTest 開啟 isReturnDefaultValues）。
 */
private fun roomRepositories(): TestRepositories {
    val database = Room.inMemoryDatabaseBuilder<AppDatabase>(ContextWrapper(null))
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
    return TestRepositories(
        itineraries = RoomItineraryRepository(database),
        items = RoomItineraryItemRepository(database),
    )
}

class RoomItineraryRepositoryTest : ItineraryRepositoryContract() {
    override fun newRepositories() = roomRepositories()
}

class RoomItineraryItemRepositoryTest : ItineraryItemRepositoryContract() {
    override fun newRepositories() = roomRepositories()
}

class RoomDataLayerAcceptanceTest : DataLayerAcceptanceContract() {
    override fun newRepositories() = roomRepositories()
}
