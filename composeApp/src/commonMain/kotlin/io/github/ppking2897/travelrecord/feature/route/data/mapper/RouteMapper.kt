package io.github.ppking2897.travelrecord.feature.route.data.mapper

import io.github.ppking2897.travelrecord.feature.route.data.model.RouteResponse
import io.github.ppking2897.travelrecord.feature.route.data.model.RouteLocationResponse
import io.github.ppking2897.travelrecord.feature.route.domain.model.Route
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteLocation
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper.toDomain
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper.toDto

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
