package com.example.myapplication.feature.route.domain.usecase

import com.example.myapplication.feature.route.domain.model.Route
import com.example.myapplication.feature.route.domain.repository.RouteRepository

class GetRouteUseCase(
    private val repository: RouteRepository
) {
    suspend operator fun invoke(id: String): Result<Route?> {
        return repository.getRoute(id)
    }
}
