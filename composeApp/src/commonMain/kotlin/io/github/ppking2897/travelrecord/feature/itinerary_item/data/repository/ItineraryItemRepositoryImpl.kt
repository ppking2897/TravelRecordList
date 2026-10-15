package io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository

import io.github.ppking2897.travelrecord.core.data.local.db.AppDatabase
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.local.toDomain
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.local.toEntity
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository

/**
 * 以 Room 實作的 ItineraryItemRepository，每個項目只存在項目表這一份
 *
 * 新增項目前所屬行程必須已存在。
 */
class ItineraryItemRepositoryImpl(database: AppDatabase) : ItineraryItemRepository {

    private val itemDao = database.itineraryItemDao()

    override suspend fun addItem(item: ItineraryItem): Result<ItineraryItem> = runCatching {
        itemDao.insert(item.toEntity())
        item
    }

    override suspend fun updateItem(item: ItineraryItem): Result<ItineraryItem> = runCatching {
        if (itemDao.update(item.toEntity()) == 0) {
            throw NoSuchElementException("Item not found: ${item.id}")
        }
        item
    }

    override suspend fun deleteItem(id: String): Result<Unit> = runCatching {
        itemDao.delete(id)
    }

    override suspend fun getItem(id: String): Result<ItineraryItem?> = runCatching {
        itemDao.get(id)?.toDomain()
    }

    override suspend fun getItemsByItinerary(itineraryId: String): Result<List<ItineraryItem>> = runCatching {
        itemDao.getByItinerary(itineraryId).map { it.toDomain() }
    }
}
