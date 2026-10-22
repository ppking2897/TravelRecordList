package io.github.ppking2897.travelrecord.feature.route.domain.usecase

import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository

class ExportRouteUseCase(
    private val repository: RouteRepository
) {
    suspend operator fun invoke(itineraryId: String): Result<String> {
        return repository.exportRoute(itineraryId)
    }
}
