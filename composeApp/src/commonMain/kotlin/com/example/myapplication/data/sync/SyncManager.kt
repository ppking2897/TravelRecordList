package com.example.myapplication.data.sync

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.model.ItineraryItem
import com.example.myapplication.data.storage.JsonSerializer
import com.example.myapplication.data.storage.StorageService
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * 同步標記資料模型
 */
@Serializable
data class SyncMarker(
    val entityType: String, // "itinerary" or "item"
    val entityId: String,
    val operation: String, // "create", "update", "delete"
    val timestamp: String
)

/**
 * 同步管理器
 * 處理離線資料修改和衝突解決
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class SyncManager(
    private val storageService: StorageService
) {
    companion object {
        private const val SYNC_MARKERS_KEY = "sync:markers"
    }
    
    /**
     * 標記實體需要同步
     */
    suspend fun markForSync(
        entityType: String,
        entityId: String,
        operation: String
    ): Result<Unit> {
        return try {
            val markers = getSyncMarkers().toMutableList()
            
            // 移除相同實體的舊標記
            markers.removeAll { it.entityType == entityType && it.entityId == entityId }
            
            // 添加新標記
            val newMarker = SyncMarker(
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                timestamp = kotlin.time.Clock.System.now().toString()
            )
            markers.add(newMarker)
            
            // 儲存標記
            saveSyncMarkers(markers)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 取得所有待同步的標記
     */
    suspend fun getSyncMarkers(): List<SyncMarker> {
        return try {
            val jsonData = storageService.load(SYNC_MARKERS_KEY).getOrNull()
            if (jsonData == null) {
                emptyList()
            } else {
                JsonSerializer.deserializeSyncMarkers(jsonData)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 清除指定實體的同步標記
     */
    suspend fun clearSyncMarker(entityType: String, entityId: String): Result<Unit> {
        return try {
            val markers = getSyncMarkers().toMutableList()
            markers.removeAll { it.entityType == entityType && it.entityId == entityId }
            saveSyncMarkers(markers)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 清除所有同步標記
     */
    suspend fun clearAllSyncMarkers(): Result<Unit> {
        return try {
            storageService.delete(SYNC_MARKERS_KEY)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 解決 Itinerary 衝突
     * 保留 modifiedAt 較新的版本
     */
    fun resolveItineraryConflict(
        local: Itinerary,
        remote: Itinerary
    ): Itinerary {
        return if (local.modifiedAt > remote.modifiedAt) {
            local
        } else {
            remote
        }
    }
    
    /**
     * 解決 ItineraryItem 衝突
     * 保留 modifiedAt 較新的版本
     */
    fun resolveItemConflict(
        local: ItineraryItem,
        remote: ItineraryItem
    ): ItineraryItem {
        return if (local.modifiedAt > remote.modifiedAt) {
            local
        } else {
            remote
        }
    }
    
    /**
     * 儲存同步標記
     */
    private suspend fun saveSyncMarkers(markers: List<SyncMarker>) {
        val jsonData = JsonSerializer.serializeSyncMarkers(markers)
        storageService.save(SYNC_MARKERS_KEY, jsonData)
    }
}
