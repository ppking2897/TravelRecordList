package com.example.myapplication.feature.route.data.mapper

import com.example.myapplication.feature.route.data.model.RouteResponse
import com.example.myapplication.feature.route.data.model.RouteLocationResponse
import com.example.myapplication.feature.route.domain.model.Route
import com.example.myapplication.feature.route.domain.model.RouteLocation
import com.example.myapplication.feature.itinerary_item.data.mapper.toDomain
import com.example.myapplication.feature.itinerary_item.data.mapper.toDto

/**
 * RouteLocation Response ↔ Domain 轉換
 */
object RouteLocationMapper {
    fun toDomain(dto: RouteLocationResponse): RouteLocation = RouteLocation(
        location = dto.location.toDomain(),
        order = dto.order,
        recommendedDuration = dto.recommendedDuration,
        notes = dto.notes
    )

    fun toDto(entity: RouteLocation): RouteLocationResponse = RouteLocationResponse(
        location = entity.location.toDto(),
        order = entity.order,
        recommendedDuration = entity.recommendedDuration,
        notes = entity.notes
    )
}

fun RouteLocationResponse.toDomain(): RouteLocation = RouteLocationMapper.toDomain(this)
fun RouteLocation.toDto(): RouteLocationResponse = RouteLocationMapper.toDto(this)

/**
 * Route Response ↔ Domain 轉換
 */
object RouteMapper {
    fun toDomain(dto: RouteResponse): Route = Route(
        id = dto.id,
        title = dto.title,
        locations = dto.locations.map { it.toDomain() },
        createdFrom = dto.createdFrom
    )

    fun toDto(entity: Route): RouteResponse = RouteResponse(
        id = entity.id,
        title = entity.title,
        locations = entity.locations.map { it.toDto() },
        createdFrom = entity.createdFrom
    )
}

fun RouteResponse.toDomain(): Route = RouteMapper.toDomain(this)
fun Route.toDto(): RouteResponse = RouteMapper.toDto(this)
