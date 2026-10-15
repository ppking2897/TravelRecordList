package io.github.ppking2897.travelrecord.feature.itinerary.data.repository

import io.github.ppking2897.travelrecord.core.data.local.db.AppDatabase
import io.github.ppking2897.travelrecord.feature.itinerary.data.local.toDomain
import io.github.ppking2897.travelrecord.feature.itinerary.data.local.toEntity
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.local.toDomain

/**
 * 以 Room 實作的 ItineraryRepository
 *
 * 行程的 items 一律從項目表讀出，排序與 getItemsByItinerary 相同；寫入行程時忽略 items。
 * 刪除行程時資料庫會連帶刪除它的項目。
 */
class RoomItineraryRepository(database: AppDatabase) : ItineraryRepository {

    private val itineraryDao = database.itineraryDao()
    private val itemDao = database.itineraryItemDao()

    override suspend fun createItinerary(itinerary: Itinerary): Result<Itinerary> = runCatching {
        itineraryDao.insert(itinerary.toEntity())
        itinerary
    }

    override suspend fun getItinerary(id: String): Result<Itinerary?> = runCatching {
        itineraryDao.get(id)?.toDomain(items = itemDao.getByItinerary(id).map { it.toDomain() })
    }

    override suspend fun getAllItineraries(): Result<List<Itinerary>> = runCatching {
        val itemsByItinerary = itemDao.getAll().map { it.toDomain() }.groupBy { it.itineraryId }
        itineraryDao.getAll().map { it.toDomain(items = itemsByItinerary[it.id].orEmpty()) }
    }

    override suspend fun updateItinerary(itinerary: Itinerary): Result<Itinerary> = runCatching {
        if (itineraryDao.update(itinerary.toEntity()) == 0) {
            throw NoSuchElementException("Itinerary not found: ${itinerary.id}")
        }
        itinerary
    }

    override suspend fun deleteItinerary(id: String): Result<Unit> = runCatching {
        itineraryDao.delete(id)
    }

    override suspend fun searchItineraries(query: String): Result<List<Itinerary>> = runCatching {
        val all = getAllItineraries().getOrThrow()
        if (query.isBlank()) return@runCatching all

        all.filter { itinerary ->
            itinerary.title.contains(query, ignoreCase = true) ||
                itinerary.description.contains(query, ignoreCase = true) ||
                itinerary.items.any { item ->
                    item.location.name.contains(query, ignoreCase = true) ||
                        item.activity.contains(query, ignoreCase = true)
                }
        }
    }
}
