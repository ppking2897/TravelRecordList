package io.github.ppking2897.travelrecord.feature.route.data.repository

import io.github.ppking2897.travelrecord.feature.route.data.mapper.toDto
import io.github.ppking2897.travelrecord.core.data.local.JsonSerializer
import io.github.ppking2897.travelrecord.core.data.local.StorageService
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.route.domain.model.Route
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteLocation
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository

/**
 * RouteRepository 實作
 *
 * 路線從行程現算；[StorageService] 只用來清掉舊版存下的路線。
 */
class RouteRepositoryImpl(
    private val storageService: StorageService,
    private val itineraryRepository: ItineraryRepository
) : RouteRepository {

    companion object {
        /** 舊版存路線用的鍵開頭，索引 route:index 也在內 */
        private const val STORED_ROUTE_KEY_PREFIX = "route:"
    }

    override suspend fun getRouteForItinerary(itineraryId: String): Result<Route> {
        return try {
            val itinerary = itineraryRepository.getItinerary(itineraryId).getOrNull()
                ?: return Result.failure(Exception("找不到路線"))

            val uniqueLocations = itinerary.items
                .map { it.location.name }
                .distinct()

            if (uniqueLocations.size < 2) {
                return Result.failure(
                    Exception("至少要有兩個不同的地點才能生成路線")
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
                            notes = ""
                        )
                    )
                    seenLocations.add(item.location.name)
                }
            }

            Result.success(
                Route(
                    title = itinerary.title,
                    locations = routeLocations,
                    createdFrom = itineraryId
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportRoute(itineraryId: String): Result<String> =
        getRouteForItinerary(itineraryId).map { route ->
            JsonSerializer.serializeRoute(route.toDto())
        }

    override suspend fun deleteStoredRoutes(): Result<Unit> {
        return try {
            storageService.getAllKeys().getOrThrow()
                .filter { it.startsWith(STORED_ROUTE_KEY_PREFIX) }
                .forEach { key -> storageService.delete(key).getOrThrow() }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
