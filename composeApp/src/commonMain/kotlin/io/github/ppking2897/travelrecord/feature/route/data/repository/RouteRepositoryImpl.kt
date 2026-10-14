package io.github.ppking2897.travelrecord.feature.route.data.repository

import io.github.ppking2897.travelrecord.feature.route.data.mapper.toDto
import io.github.ppking2897.travelrecord.feature.route.data.mapper.toDomain
import io.github.ppking2897.travelrecord.core.data.local.JsonSerializer
import io.github.ppking2897.travelrecord.core.data.local.StorageService
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.route.domain.model.Route
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteLocation
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository

/**
 * RouteRepository 實作
 * 使用 StorageService 儲存資料
 */
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
            val uniqueLocations = route.locations.map { it.location.name }.distinct()
            if (uniqueLocations.size < 2) {
                return Result.failure(
                    Exception("Route must contain at least two unique locations")
                )
            }

            val key = "$ROUTE_KEY_PREFIX${route.id}"
            val dto = route.toDto()
            val jsonData = JsonSerializer.serializeRoute(dto)
            storageService.save(key, jsonData).getOrThrow()

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
                val dto = JsonSerializer.deserializeRoute(jsonData)
                Result.success(dto.toDomain())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportRoute(id: String): Result<String> {
        return try {
            val route = getRoute(id).getOrNull()
                ?: return Result.failure(Exception("Route not found: $id"))

            val dto = route.toDto()
            val jsonData = JsonSerializer.serializeRoute(dto)
            Result.success(jsonData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRouteFromItinerary(
        itineraryId: String,
        routeId: String
    ): Result<Route> {
        return try {
            val itinerary = itineraryRepository.getItinerary(itineraryId).getOrNull()
                ?: return Result.failure(Exception("Itinerary not found: $itineraryId"))

            val uniqueLocations = itinerary.items
                .map { it.location.name }
                .distinct()

            if (uniqueLocations.size < 2) {
                return Result.failure(
                    Exception("Itinerary must contain at least two unique locations to create a route")
                )
            }

            val sortedItems = itinerary.items.sortedWith(
                compareBy<ItineraryItem> { it.date }
                    .thenBy { it.primaryTime() }
            )

            val routeLocations = mutableListOf<RouteLocation>()
            val seenLocations = mutableSetOf<String>()
            var order = 0

            for (item in sortedItems) {
                if (!seenLocations.contains(item.location.name)) {
                    routeLocations.add(
                        RouteLocation(
                            location = item.location,
                            order = order++,
                            recommendedDuration = null,
                            notes = ""
                        )
                    )
                    seenLocations.add(item.location.name)
                }
            }

            val route = Route(
                id = routeId,
                title = itinerary.title,
                locations = routeLocations,
                createdFrom = itineraryId
            )

            createRoute(route)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    private suspend fun updateIndex(transform: (List<String>) -> List<String>) {
        try {
            val currentIds = getRouteIds()
            val newIds = transform(currentIds).distinct()
            val jsonData = JsonSerializer.serializeStringList(newIds)
            storageService.save(ROUTE_INDEX_KEY, jsonData)
        } catch (e: Exception) {
            // 索引更新失敗不應影響主要邏輯
        }
    }
}
