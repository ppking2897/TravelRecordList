package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.storage.JsonSerializer
import com.example.myapplication.data.storage.StorageService

/**
 * ItineraryRepository 的實作
 * 使用 StorageService 進行資料持久化
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class ItineraryRepositoryImpl(
    private val storageService: StorageService
) : ItineraryRepository {
    
    companion object {
        private const val ITINERARY_KEY_PREFIX = "itinerary:"
        private const val ITINERARY_INDEX_KEY = "itinerary:index"
    }
    
    override suspend fun createItinerary(itinerary: Itinerary): Result<Itinerary> {
        return try {
            // 儲存 itinerary
            val key = "$ITINERARY_KEY_PREFIX${itinerary.id}"
            val jsonData = JsonSerializer.serializeItinerary(itinerary)
            storageService.save(key, jsonData).getOrThrow()
            
            // 更新索引
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
                val itinerary = JsonSerializer.deserializeItinerary(jsonData)
                Result.success(itinerary)
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
            
            // 按 createdAt 降序排序（最新的在前）
            val sorted = itineraries.sortedByDescending { it.createdAt }
            
            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateItinerary(itinerary: Itinerary): Result<Itinerary> {
        return try {
            // 檢查 itinerary 是否存在
            if (getItinerary(itinerary.id).getOrNull() == null) {
                return Result.failure(Exception("Itinerary not found: ${itinerary.id}"))
            }

            // 更新 itinerary
            val key = "$ITINERARY_KEY_PREFIX${itinerary.id}"
            val jsonData = JsonSerializer.serializeItinerary(itinerary)
            storageService.save(key, jsonData).getOrThrow()
            
            Result.success(itinerary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteItinerary(id: String): Result<Unit> {
        return try {
            // 刪除 itinerary
            val key = "$ITINERARY_KEY_PREFIX$id"
            storageService.delete(key).getOrThrow()
            
            // 從索引中移除
            updateIndex { ids -> ids - id }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchItineraries(query: String): Result<List<Itinerary>> {
        return try {
            // 如果查詢為空，返回所有 itineraries
            if (query.isBlank()) {
                return getAllItineraries()
            }
            
            val allItineraries = getAllItineraries().getOrThrow()
            val queryLower = query.lowercase()
            
            // 搜尋 title、description 和所有 items 的 location 和 activity
            val filtered = allItineraries.filter { itinerary ->
                // 搜尋 title
                itinerary.title.lowercase().contains(queryLower) ||
                // 搜尋 description
                itinerary.description.lowercase().contains(queryLower) ||
                // 搜尋 items 的 location name
                itinerary.items.any { item ->
                    item.location.name.lowercase().contains(queryLower)
                } ||
                // 搜尋 items 的 activity
                itinerary.items.any { item ->
                    item.activity.lowercase().contains(queryLower)
                }
            }
            
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 取得所有 itinerary IDs
     */
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
    
    /**
     * 更新 itinerary IDs 索引
     */
    private suspend fun updateIndex(transform: (List<String>) -> List<String>) {
        try {
            val currentIds = getItineraryIds()
            val newIds = transform(currentIds).distinct()
            val jsonData = JsonSerializer.serializeStringList(newIds)
            storageService.save(ITINERARY_INDEX_KEY, jsonData)
        } catch (e: Exception) {
            // 索引更新失敗不應該影響主要操作
            // 可以記錄錯誤日誌
        }
    }
}
