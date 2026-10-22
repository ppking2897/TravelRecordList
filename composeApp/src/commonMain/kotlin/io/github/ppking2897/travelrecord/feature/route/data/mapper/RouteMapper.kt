package io.github.ppking2897.travelrecord.feature.route.data.mapper

import io.github.ppking2897.travelrecord.feature.route.data.model.RouteResponse
import io.github.ppking2897.travelrecord.feature.route.data.model.RouteLocationResponse
import io.github.ppking2897.travelrecord.feature.route.domain.model.Route
import io.github.ppking2897.travelrecord.feature.route.domain.model.RouteLocation
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper.toDto

/**
 * RouteLocation Domain → Response 轉換（匯出用）
 */
object RouteLocationMapper {
    fun toDto(entity: RouteLocation): RouteLocationResponse = RouteLocationResponse(
        location = entity.location.toDto(),
        order = entity.order,
        notes = entity.notes
    )
}

fun RouteLocation.toDto(): RouteLocationResponse = RouteLocationMapper.toDto(this)

/**
 * Route Domain → Response 轉換（匯出用）
 */
object RouteMapper {
    fun toDto(entity: Route): RouteResponse = RouteResponse(
        title = entity.title,
        locations = entity.locations.map { it.toDto() },
        createdFrom = entity.createdFrom
    )
}

fun Route.toDto(): RouteResponse = RouteMapper.toDto(this)
