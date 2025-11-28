package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Route
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 測試用的假 RouteRepository 實作
 */
class FakeRouteRepository : RouteRepository {
    
    private val routes = mutableMapOf<String, Route>()
    var shouldFail = false
    var failureMessage = "Test failure"
    
    override suspend fun createRoute(route: Route): Result<Route> {
        return if (shouldFail) {
            Result.failure(Exception(failureMessage))
        } else {
            routes[route.id] = route
            Result.success(route)
        }
    }
    
    override suspend fun getRoute(id: String): Result<Route?> {
        return if (shouldFail) {
            Result.failure(Exception(failureMessage))
        } else {
            Result.success(routes[id])
        }
    }
    
    override suspend fun exportRoute(id: String): Result<String> {
        return if (shouldFail) {
            Result.failure(Exception(failureMessage))
        } else {
            val route = routes[id]
            if (route != null) {
                Result.success(Json.encodeToString(route))
            } else {
                Result.failure(Exception("Route not found"))
            }
        }
    }
    
    fun addRoute(route: Route) {
        routes[route.id] = route
    }
    
    fun clear() {
        routes.clear()
        shouldFail = false
    }
}
