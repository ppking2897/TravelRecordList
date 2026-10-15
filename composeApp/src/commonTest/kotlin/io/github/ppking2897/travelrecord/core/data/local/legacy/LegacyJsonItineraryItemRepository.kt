@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.core.data.local.legacy

import io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper.toDto
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper.toDomain
import io.github.ppking2897.travelrecord.core.data.local.JsonSerializer
import io.github.ppking2897.travelrecord.core.data.local.StorageService
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository

/**
 * 舊版的項目儲存：每個項目一筆 JSON 存在鍵值儲存（StorageService）
 *
 * App 已改用 Room，這份實作只留在測試裡，用來產生舊格式資料給搬移測試使用。
 */
class LegacyJsonItineraryItemRepository(
    private val storageService: StorageService
) : ItineraryItemRepository {

    companion object {
        private const val ITEM_KEY_PREFIX = "item:"
        private const val ITEM_INDEX_KEY = "item:index"
    }

    override suspend fun addItem(item: ItineraryItem): Result<ItineraryItem> {
        return try {
            val key = "$ITEM_KEY_PREFIX${item.id}"
            val dto = item.toDto()
            val jsonData = JsonSerializer.serializeItineraryItem(dto)
            storageService.save(key, jsonData).getOrThrow()

            updateIndex { ids -> ids + item.id }

            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateItem(item: ItineraryItem): Result<ItineraryItem> {
        return try {
            val key = "$ITEM_KEY_PREFIX${item.id}"
            val existing = storageService.load(key).getOrNull()
            if (existing == null) {
                return Result.failure(Exception("Item not found: ${item.id}"))
            }

            val dto = item.toDto()
            val jsonData = JsonSerializer.serializeItineraryItem(dto)
            storageService.save(key, jsonData).getOrThrow()

            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            val key = "$ITEM_KEY_PREFIX$id"
            storageService.delete(key).getOrThrow()

            updateIndex { ids -> ids - id }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getItem(id: String): Result<ItineraryItem?> {
        return try {
            val key = "$ITEM_KEY_PREFIX$id"
            val jsonData = storageService.load(key).getOrNull()
            if (jsonData == null) {
                Result.success(null)
            } else {
                val dto = JsonSerializer.deserializeItineraryItem(jsonData)
                Result.success(dto.toDomain())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getItemsByItinerary(itineraryId: String): Result<List<ItineraryItem>> {
        return try {
            val allItems = getAllItems()

            val filtered = allItems.filter { it.itineraryId == itineraryId }

            val sorted = filtered.sortedWith(
                compareBy<ItineraryItem> { it.date }
                    .thenBy { it.primaryTime() }
            )

            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getAllItems(): List<ItineraryItem> {
        val ids = getItemIds()
        val items = mutableListOf<ItineraryItem>()

        for (id in ids) {
            try {
                val key = "$ITEM_KEY_PREFIX$id"
                val jsonData = storageService.load(key).getOrNull()
                if (jsonData != null) {
                    val dto = JsonSerializer.deserializeItineraryItem(jsonData)
                    items.add(dto.toDomain())
                }
            } catch (e: Exception) {
                continue
            }
        }

        return items
    }

    private suspend fun getItemIds(): List<String> {
        return try {
            val jsonData = storageService.load(ITEM_INDEX_KEY).getOrNull()
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
            val currentIds = getItemIds()
            val newIds = transform(currentIds).distinct()
            val jsonData = JsonSerializer.serializeStringList(newIds)
            storageService.save(ITEM_INDEX_KEY, jsonData)
        } catch (e: Exception) {
            // 索引更新失敗不應影響主要邏輯
        }
    }
}
