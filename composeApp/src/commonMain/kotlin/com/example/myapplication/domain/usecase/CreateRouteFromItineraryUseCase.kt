package com.example.myapplication.domain.usecase

import com.example.myapplication.data.model.Route
import com.example.myapplication.data.repository.RouteRepositoryImpl
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * 從行程建立路線的 Use Case
 */
@OptIn(ExperimentalUuidApi::class)
class CreateRouteFromItineraryUseCase(
    private val routeRepository: RouteRepositoryImpl
) {
    suspend operator fun invoke(itineraryId: String): Result<Route> {
        return try {
            val routeId = Uuid.random().toString()
            routeRepository.createRouteFromItinerary(itineraryId, routeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
