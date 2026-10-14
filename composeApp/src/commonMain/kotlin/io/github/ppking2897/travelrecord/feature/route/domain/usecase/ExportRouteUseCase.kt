package io.github.ppking2897.travelrecord.feature.route.domain.usecase

import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository

class ExportRouteUseCase(
    private val repository: RouteRepository
) {
    suspend operator fun invoke(routeId: String): Result<String> {
        return repository.exportRoute(routeId)
    }
}
