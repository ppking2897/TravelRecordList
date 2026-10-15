package io.github.ppking2897.travelrecord.core.data.local

import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.ItineraryRepositoryContract
import io.github.ppking2897.travelrecord.core.data.local.legacy.LegacyJsonItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.ItineraryItemRepositoryContract
import io.github.ppking2897.travelrecord.core.data.local.legacy.LegacyJsonItineraryItemRepository
import io.github.ppking2897.travelrecord.test.TestRepositories

/** 舊版 JSON 鍵值儲存的 Repository（搬移測試用來產生舊格式資料），也必須符合同一份合約 */
private fun jsonRepositories(): TestRepositories {
    val storage = InMemoryStorageService()
    return TestRepositories(
        itineraries = LegacyJsonItineraryRepository(storage),
        items = LegacyJsonItineraryItemRepository(storage),
    )
}

class JsonItineraryRepositoryTest : ItineraryRepositoryContract() {
    override fun newRepositories() = jsonRepositories()
}

class JsonItineraryItemRepositoryTest : ItineraryItemRepositoryContract() {
    override fun newRepositories() = jsonRepositories()
}
