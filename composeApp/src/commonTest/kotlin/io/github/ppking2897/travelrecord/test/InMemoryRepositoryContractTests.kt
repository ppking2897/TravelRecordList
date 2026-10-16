package io.github.ppking2897.travelrecord.test

import io.github.ppking2897.travelrecord.core.data.DataLayerAcceptanceContract
import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.ItineraryRepositoryContract
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.ItineraryItemRepositoryContract

/** 測試用的記憶體 Repository 也必須符合資料庫版的合約與驗收測試，ViewModel 測試的結果才可信 */
private fun inMemoryRepositories(): TestRepositories {
    val items = InMemoryItineraryItemRepository()
    return TestRepositories(itineraries = InMemoryItineraryRepository(items), items = items)
}

class InMemoryItineraryRepositoryTest : ItineraryRepositoryContract() {
    override fun newRepositories() = inMemoryRepositories()
}

class InMemoryItineraryItemRepositoryTest : ItineraryItemRepositoryContract() {
    override fun newRepositories() = inMemoryRepositories()
}

class InMemoryDataLayerAcceptanceTest : DataLayerAcceptanceContract() {
    override fun newRepositories() = inMemoryRepositories()
}
