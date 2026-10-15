package io.github.ppking2897.travelrecord.core.data.local

import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.ItineraryRepositoryContract
import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.ItineraryRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.ItineraryItemRepositoryContract
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.ItineraryItemRepositoryImpl
import io.github.ppking2897.travelrecord.test.TestRepositories

/** 以 JSON 鍵值儲存（StorageService）實作的 Repository，資料放在記憶體中 */
private fun jsonRepositories(): TestRepositories {
    val storage = InMemoryStorageService()
    return TestRepositories(
        itineraries = ItineraryRepositoryImpl(storage),
        items = ItineraryItemRepositoryImpl(storage),
    )
}

class JsonItineraryRepositoryTest : ItineraryRepositoryContract() {
    override fun newRepositories() = jsonRepositories()
}

class JsonItineraryItemRepositoryTest : ItineraryItemRepositoryContract() {
    override fun newRepositories() = jsonRepositories()
}
