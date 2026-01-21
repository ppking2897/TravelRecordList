package com.example.myapplication.data.repository

import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.storage.JsonSerializer
import com.example.myapplication.data.storage.StorageService
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime

/**
 * ItineraryItemRepository 的實作
 * 使用 StorageService 進行資料持久化
 */
@OptIn(ExperimentalTime::class)
class ItineraryItemRepositoryImpl(
    private val storageService: StorageService
) : ItineraryItemRepository {
    
    companion object {
        private const val ITEM_KEY_PREFIX = "item:"
        private const val ITEM_INDEX_KEY = "item:index"
    }
    
    override suspend fun addItem(item: ItineraryItem): Result<ItineraryItem> {
        return try {
            // 儲存 item
            val key = "$ITEM_KEY_PREFIX${item.id}"
            val jsonData = JsonSerializer.serializeItineraryItem(item)
            storageService.save(key, jsonData).getOrThrow()
            
            // 更新索引
            updateIndex { ids -> ids + item.id }
            
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateItem(item: ItineraryItem): Result<ItineraryItem> {
        return try {
            // 檢查 item 是否存在
            val key = "$ITEM_KEY_PREFIX${item.id}"
            val existing = storageService.load(key).getOrNull()
            if (existing == null) {
                return Result.failure(Exception("Item not found: ${item.id}"))
            }
            
            // 更新 item
            val jsonData = JsonSerializer.serializeItineraryItem(item)
            storageService.save(key, jsonData).getOrThrow()
            
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteItem(id: String): Result<Unit> {
        return try {
            // 刪除 item
            val key = "$ITEM_KEY_PREFIX$id"
            storageService.delete(key).getOrThrow()

            // 從索引中移除
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
                val item = JsonSerializer.deserializeItineraryItem(jsonData)
                Result.success(item)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }    
    override suspend fun getItemsByItinerary(itineraryId: String): Result<List<ItineraryItem>> {
        return try {
            val allItems = getAllItems()
            
            // 過濾屬於指定 itinerary 的 items
            val filtered = allItems.filter { it.itineraryId == itineraryId }
            
            // 按時間順序排序（date 升序，相同 date 則按 primaryTime 升序）
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
            
            // 過濾指定 location 的 items
            val filtered = allItems.filter { 
                it.location.name.equals(locationName, ignoreCase = false)
            }
            
            // 按日期排序
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
            
            // 過濾日期範圍內的 items
            val filtered = allItems.filter { item ->
                item.date >= start && item.date <= end
            }
            
            // 按時間順序排序
            val sorted = filtered.sortedWith(
                compareBy<ItineraryItem> { it.date }
                    .thenBy { it.primaryTime() }
            )
            
            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 取得所有 items
     */
    private suspend fun getAllItems(): List<ItineraryItem> {
        val ids = getItemIds()
        val items = mutableListOf<ItineraryItem>()
        
        for (id in ids) {
            try {
                val key = "$ITEM_KEY_PREFIX$id"
                val jsonData = storageService.load(key).getOrNull()
                if (jsonData != null) {
                    val item = JsonSerializer.deserializeItineraryItem(jsonData)
                    items.add(item)
                }
            } catch (e: Exception) {
                // 跳過無法載入的 item
                continue
            }
        }
        
        return items
    }
    
    /**
     * 取得所有 item IDs
     */
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
    
    /**
     * 更新 item IDs 索引
     */
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
    
    /**
     * 切換項目的完成狀態
     * 
     * @param itemId 項目 ID
     * @param currentTimestamp 當前時間戳（用於設定 completedAt 和 modifiedAt）
     * @return 更新後的項目或錯誤
     */
    override suspend fun toggleCompletion(
        itemId: String,
        currentTimestamp: kotlinx.datetime.Instant
    ): Result<ItineraryItem> {
        return try {
            // 載入現有 item
            val key = "$ITEM_KEY_PREFIX$itemId"
            val jsonData = storageService.load(key).getOrNull()
                ?: return Result.failure(Exception("Item not found: $itemId"))
            
            val item = JsonSerializer.deserializeItineraryItem(jsonData)
            
            // 切換完成狀態
            val updatedItem = if (item.isCompleted) {
                // 取消完成：設為未完成，清除 completedAt
                item.copy(
                    isCompleted = false,
                    completedAt = null,
                    modifiedAt = currentTimestamp
                )
            } else {
                // 標記為完成：設為已完成，記錄 completedAt
                item.copy(
                    isCompleted = true,
                    completedAt = currentTimestamp,
                    modifiedAt = currentTimestamp
                )
            }
            
            // 儲存更新後的 item
            updateItem(updatedItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 計算指定行程的完成進度
     * 
     * @param itineraryId 行程 ID
     * @return 完成百分比（0.0 到 100.0）
     */
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
}
