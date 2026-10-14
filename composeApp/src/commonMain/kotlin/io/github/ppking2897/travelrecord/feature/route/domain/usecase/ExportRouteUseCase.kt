package com.example.myapplication.feature.route.domain.usecase

import com.example.myapplication.feature.route.domain.repository.RouteRepository

class ExportRouteUseCase(
    private val repository: RouteRepository
) {
    suspend operator fun invoke(routeId: String): Result<String> {
        return repository.exportRoute(routeId)
    }
}
