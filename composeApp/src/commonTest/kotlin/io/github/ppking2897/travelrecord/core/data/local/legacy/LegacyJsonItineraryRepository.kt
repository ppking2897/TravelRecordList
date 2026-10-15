@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.core.data.local.legacy

import io.github.ppking2897.travelrecord.feature.itinerary.data.mapper.toDto
import io.github.ppking2897.travelrecord.feature.itinerary.data.mapper.toDomain
import io.github.ppking2897.travelrecord.core.data.local.JsonSerializer
import io.github.ppking2897.travelrecord.core.data.local.StorageService
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository

/**
 * 舊版的行程儲存：每個行程一筆 JSON 存在鍵值儲存（StorageService）
 *
 * App 已改用 Room，這份實作只留在測試裡，用來產生舊格式資料給搬移測試使用。
 */
class LegacyJsonItineraryRepository(
    private val storageService: StorageService
) : ItineraryRepository {

    companion object {
        private const val ITINERARY_KEY_PREFIX = "itinerary:"
        private const val ITINERARY_INDEX_KEY = "itinerary:index"
    }

    override suspend fun createItinerary(itinerary: Itinerary): Result<Itinerary> {
        return try {
            val key = "$ITINERARY_KEY_PREFIX${itinerary.id}"
            val dto = itinerary.toDto()
            val jsonData = JsonSerializer.serializeItinerary(dto)
            storageService.save(key, jsonData).getOrThrow()

            updateIndex { ids -> ids + itinerary.id }

            Result.success(itinerary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getItinerary(id: String): Result<Itinerary?> {
        return try {
            val key = "$ITINERARY_KEY_PREFIX$id"
            val jsonData = storageService.load(key).getOrThrow()

            if (jsonData == null) {
                Result.success(null)
            } else {
                val dto = JsonSerializer.deserializeItinerary(jsonData)
                Result.success(dto.toDomain())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllItineraries(): Result<List<Itinerary>> {
        return try {
            val ids = getItineraryIds()
            val itineraries = mutableListOf<Itinerary>()

            for (id in ids) {
                val result = getItinerary(id)
                result.getOrNull()?.let { itineraries.add(it) }
            }

            val sorted = itineraries.sortedByDescending { it.createdAt }

            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateItinerary(itinerary: Itinerary): Result<Itinerary> {
        return try {
            if (getItinerary(itinerary.id).getOrNull() == null) {
                return Result.failure(Exception("Itinerary not found: ${itinerary.id}"))
            }

            val key = "$ITINERARY_KEY_PREFIX${itinerary.id}"
            val dto = itinerary.toDto()
            val jsonData = JsonSerializer.serializeItinerary(dto)
            storageService.save(key, jsonData).getOrThrow()

            Result.success(itinerary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteItinerary(id: String): Result<Unit> {
        return try {
            val key = "$ITINERARY_KEY_PREFIX$id"
            storageService.delete(key).getOrThrow()

            updateIndex { ids -> ids - id }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchItineraries(query: String): Result<List<Itinerary>> {
        return try {
            if (query.isBlank()) {
                return getAllItineraries()
            }

            val allItineraries = getAllItineraries().getOrThrow()
            val queryLower = query.lowercase()

            val filtered = allItineraries.filter { itinerary ->
                itinerary.title.lowercase().contains(queryLower) ||
                    itinerary.description.lowercase().contains(queryLower) ||
                    itinerary.items.any { item ->
                        item.location.name.lowercase().contains(queryLower)
                    } ||
                    itinerary.items.any { item ->
                        item.activity.lowercase().contains(queryLower)
                    }
            }

            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getItineraryIds(): List<String> {
        return try {
            val jsonData = storageService.load(ITINERARY_INDEX_KEY).getOrNull()
            if (jsonData == null) {
                emptyList()
            } else {
                JsonSerializer.deserializeStringList(jsonData)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun updateIndex(transform: (List<String>) -> List<String>) {
        try {
            val currentIds = getItineraryIds()
            val newIds = transform(currentIds).distinct()
            val jsonData = JsonSerializer.serializeStringList(newIds)
            storageService.save(ITINERARY_INDEX_KEY, jsonData)
        } catch (e: Exception) {
            // 索引更新失敗不應影響主要邏輯
        }
    }
}
