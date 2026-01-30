@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.data.repository

import com.example.myapplication.data.mapper.toDto
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.data.storage.JsonSerializer
import com.example.myapplication.data.storage.StorageService
import com.example.myapplication.domain.entity.ItineraryItem
import com.example.myapplication.domain.repository.ItineraryItemRepository
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * ItineraryItemRepository 的實作
 * 使用 StorageService 進行資料持久化
 */
class ItineraryItemRepositoryImpl(
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
                Result.success(dto.toEntity())
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

    override suspend fun getItemsByLocation(locationName: String): Result<List<ItineraryItem>> {
        return try {
            val allItems = getAllItems()

            val filtered = allItems.filter {
                it.location.name.equals(locationName, ignoreCase = false)
            }

            val sorted = filtered.sortedWith(
                compareBy<ItineraryItem> { it.date }
                    .thenBy { it.primaryTime() }
            )

            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getItemsByDateRange(
        start: LocalDate,
        end: LocalDate
    ): Result<List<ItineraryItem>> {
        return try {
            val allItems = getAllItems()

            val filtered = allItems.filter { item ->
                item.date >= start && item.date <= end
            }

            val sorted = filtered.sortedWith(
                compareBy<ItineraryItem> { it.date }
                    .thenBy { it.primaryTime() }
            )

            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleCompletion(
        itemId: String,
        currentTimestamp: Instant
    ): Result<ItineraryItem> {
        return try {
            val key = "$ITEM_KEY_PREFIX$itemId"
            val jsonData = storageService.load(key).getOrNull()
                ?: return Result.failure(Exception("Item not found: $itemId"))

            val dto = JsonSerializer.deserializeItineraryItem(jsonData)
            val item = dto.toEntity()

            val updatedItem = if (item.isCompleted) {
                item.copy(
                    isCompleted = false,
                    completedAt = null,
                    modifiedAt = currentTimestamp
                )
            } else {
                item.copy(
                    isCompleted = true,
                    completedAt = currentTimestamp,
                    modifiedAt = currentTimestamp
                )
            }

            updateItem(updatedItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateProgress(itineraryId: String): Result<Float> {
        return try {
            val items = getItemsByItinerary(itineraryId).getOrThrow()

            if (items.isEmpty()) {
                return Result.success(0f)
            }

            val completedCount = items.count { it.isCompleted }
            val totalCount = items.size
            val progress = (completedCount.toFloat() / totalCount.toFloat()) * 100f

            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reorderItems(itineraryId: String, itemIds: List<String>): Result<Unit> {
        return try {
            // 更新索引以保持新順序
            // 注意：此實作保存項目的排序順序
            // 在實際應用中可能需要為每個項目添加 sortOrder 欄位
            val allIds = getItemIds()
            val otherIds = allIds.filter { it !in itemIds }
            val newOrder = itemIds + otherIds

            val jsonData = JsonSerializer.serializeStringList(newOrder)
            storageService.save(ITEM_INDEX_KEY, jsonData)

            Result.success(Unit)
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
                    items.add(dto.toEntity())
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
            // 索引更新失敗不應該影響主要操作
        }
    }
}
