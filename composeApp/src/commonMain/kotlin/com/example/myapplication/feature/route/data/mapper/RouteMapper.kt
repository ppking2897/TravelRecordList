package com.example.myapplication.feature.route.data.mapper

import com.example.myapplication.feature.route.data.dto.RouteDto
import com.example.myapplication.feature.route.data.dto.RouteLocationDto
import com.example.myapplication.feature.route.domain.model.Route
import com.example.myapplication.feature.route.domain.model.RouteLocation
import com.example.myapplication.feature.itinerary_item.data.mapper.toDomain
import com.example.myapplication.feature.itinerary_item.data.mapper.toDto

/**
 * RouteLocation DTO ??Entity è½‰æ?
 */
object RouteLocationMapper {
    fun toDomain(dto: RouteLocationDto): RouteLocation = RouteLocation(
        location = dto.location.toDomain(),
        order = dto.order,
        recommendedDuration = dto.recommendedDuration,
        notes = dto.notes
    )

    fun toDto(entity: RouteLocation): RouteLocationDto = RouteLocationDto(
        location = entity.location.toDto(),
        order = entity.order,
        recommendedDuration = entity.recommendedDuration,
        notes = entity.notes
    )
}

fun RouteLocationDto.toDomain(): RouteLocation = RouteLocationMapper.toDomain(this)
fun RouteLocation.toDto(): RouteLocationDto = RouteLocationMapper.toDto(this)

/**
 * Route DTO ??Entity è½‰æ?
 */
object RouteMapper {
    fun toDomain(dto: RouteDto): Route = Route(
        id = dto.id,
        title = dto.title,
        locations = dto.locations.map { it.toDomain() },
        createdFrom = dto.createdFrom
    )

    fun toDto(entity: Route): RouteDto = RouteDto(
        id = entity.id,
        title = entity.title,
        locations = entity.locations.map { it.toDto() },
        createdFrom = entity.createdFrom
    )
}

fun RouteDto.toDomain(): Route = RouteMapper.toDomain(this)
fun Route.toDto(): RouteDto = RouteMapper.toDto(this)
