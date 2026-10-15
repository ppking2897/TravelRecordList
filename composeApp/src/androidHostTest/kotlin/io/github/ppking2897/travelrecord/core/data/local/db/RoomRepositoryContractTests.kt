package io.github.ppking2897.travelrecord.core.data.local.db

import io.github.ppking2897.travelrecord.core.data.DataLayerAcceptanceContract
import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.ItineraryRepositoryContract
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.ItineraryItemRepositoryContract

/** 以 Room 實作的 Repository，資料庫開在記憶體中 */
class RoomItineraryRepositoryTest : ItineraryRepositoryContract() {
    override fun newRepositories() = inMemoryAppDatabase().repositories()
}

class RoomItineraryItemRepositoryTest : ItineraryItemRepositoryContract() {
    override fun newRepositories() = inMemoryAppDatabase().repositories()
}

class RoomDataLayerAcceptanceTest : DataLayerAcceptanceContract() {
    override fun newRepositories() = inMemoryAppDatabase().repositories()
}
