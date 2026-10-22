package io.github.ppking2897.travelrecord.feature.route.domain.usecase

import io.github.ppking2897.travelrecord.feature.route.domain.model.Route
import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository

/**
 * 依行程目前的項目排出路線
 */
class GetRouteForItineraryUseCase(
    private val repository: RouteRepository
) {
    suspend operator fun invoke(itineraryId: String): Result<Route> {
        return repository.getRouteForItinerary(itineraryId)
    }
}
