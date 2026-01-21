package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Itinerary
import com.example.myapplication.data.model.Route
import com.example.myapplication.data.model.RouteLocation
import com.example.myapplication.data.storage.JsonSerializer
import com.example.myapplication.data.storage.StorageService

/**
 * RouteRepository 的實作
 * 使用 StorageService 進行資料持久化
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class RouteRepositoryImpl(
    private val storageService: StorageService,
    private val itineraryRepository: ItineraryRepository
) : RouteRepository {
    
    companion object {
        private const val ROUTE_KEY_PREFIX = "route:"
        private const val ROUTE_INDEX_KEY = "route:index"
    }
    
    override suspend fun createRoute(route: Route): Result<Route> {
        return try {
            // 驗證 route 至少有兩個 unique locations
            val uniqueLocations = route.locations.map { it.location.name }.distinct()
            if (uniqueLocations.size < 2) {
                return Result.failure(
                    Exception("Route must contain at least two unique locations")
                )
            }
            
            // 儲存 route
            val key = "$ROUTE_KEY_PREFIX${route.id}"
            val jsonData = JsonSerializer.serializeRoute(route)
            storageService.save(key, jsonData).getOrThrow()
            
            // 更新索引
            updateIndex { ids -> ids + route.id }
            
            Result.success(route)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRoute(id: String): Result<Route?> {
        return try {
            val key = "$ROUTE_KEY_PREFIX$id"
            val jsonData = storageService.load(key).getOrNull()
            
            if (jsonData == null) {
                Result.success(null)
            } else {
                val route = JsonSerializer.deserializeRoute(jsonData)
                Result.success(route)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun exportRoute(id: String): Result<String> {
        return try {
            val route = getRoute(id).getOrNull()
                ?: return Result.failure(Exception("Route not found: $id"))
            
            val jsonData = JsonSerializer.serializeRoute(route)
            Result.success(jsonData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 從 itinerary 生成 route
     * 
     * @param itineraryId 行程 ID
     * @param routeId 新 route 的 ID
     * @return 生成的 route 或錯誤
     */
    override suspend fun createRouteFromItinerary(
        itineraryId: String,
        routeId: String
    ): Result<Route> {
        return try {
            // 取得 itinerary
            val itinerary = itineraryRepository.getItinerary(itineraryId).getOrNull()
                ?: return Result.failure(Exception("Itinerary not found: $itineraryId"))
            
            // 驗證至少有兩個 unique locations
            val uniqueLocations = itinerary.items
                .map { it.location.name }
                .distinct()
            
            if (uniqueLocations.size < 2) {
                return Result.failure(
                    Exception("Itinerary must contain at least two unique locations to create a route")
                )
            }
            
            // 按時間順序排序 items
            val sortedItems = itinerary.items.sortedWith(
                compareBy<com.example.myapplication.data.model.ItineraryItem> { it.date }
                    .thenBy { it.primaryTime() }
            )
            
            // 生成 route locations（去重但保持順序）
            val routeLocations = mutableListOf<RouteLocation>()
            val seenLocations = mutableSetOf<String>()
            var order = 0
            
            for (item in sortedItems) {
                if (!seenLocations.contains(item.location.name)) {
                    routeLocations.add(
                        RouteLocation(
                            location = item.location,
                            order = order++,
                            recommendedDuration = null, // 可以後續計算
                            notes = ""
                        )
                    )
                    seenLocations.add(item.location.name)
                }
            }
            
            // 建立 route
            val route = Route(
                id = routeId,
                title = itinerary.title,
                locations = routeLocations,
                createdFrom = itineraryId
            )
            
            // 儲存 route
            createRoute(route)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 取得所有 route IDs
     */
    private suspend fun getRouteIds(): List<String> {
        return try {
            val jsonData = storageService.load(ROUTE_INDEX_KEY).getOrNull()
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
     * 更新 route IDs 索引
     */
    private suspend fun updateIndex(transform: (List<String>) -> List<String>) {
        try {
            val currentIds = getRouteIds()
            val newIds = transform(currentIds).distinct()
            val jsonData = JsonSerializer.serializeStringList(newIds)
            storageService.save(ROUTE_INDEX_KEY, jsonData)
        } catch (e: Exception) {
            // 索引更新失敗不應該影響主要操作
        }
    }
}
