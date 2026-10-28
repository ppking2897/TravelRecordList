package io.github.ppking2897.travelrecord.feature.route.data.repository

import io.github.ppking2897.travelrecord.core.data.local.StorageService
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.itemDayOrder
import io.github.ppking2897.travelrecord.feature.route.domain.model.Route
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteDay
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteLocation
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

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

            val days = itinerary.items
                .sortedWith(itemDayOrder)
                .groupBy { it.date }
                .map { (date, dayItems) ->
                    val stops = mutableListOf<RouteLocation>()
                    for (item in dayItems) {
                        // 同一天連續在同一個地點只算一站；離開再回來、或不同天再去，都照實列出
                        if (stops.lastOrNull()?.location?.name != item.location.name) {
                            stops += RouteLocation(location = item.location, order = stops.size)
                        }
                    }
                    RouteDay(date = date, dayNumber = dayNumberOf(itinerary, date), stops = stops)
                }

            Result.success(
                Route(
                    title = itinerary.title,
                    days = days,
                    createdFrom = itineraryId
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** 從開始日算第幾天，跟行程詳情的時間軸一樣；日期在行程範圍外、或行程沒有日期時回傳 null */
    private fun dayNumberOf(itinerary: Itinerary, date: LocalDate): Int? {
        val start = itinerary.startDate ?: return null
        val end = itinerary.endDate ?: return null
        if (date < start || date > end) return null
        return start.daysUntil(date) + 1
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
