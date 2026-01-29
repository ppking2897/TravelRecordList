package com.example.myapplication.data.mapper

import com.example.myapplication.data.dto.RouteDto
import com.example.myapplication.data.dto.RouteLocationDto
import com.example.myapplication.domain.entity.Route
import com.example.myapplication.domain.entity.RouteLocation

/**
 * RouteLocation DTO ↔ Entity 轉換
 */
object RouteLocationMapper {
    fun toEntity(dto: RouteLocationDto): RouteLocation = RouteLocation(
        location = dto.location.toEntity(),
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

fun RouteLocationDto.toEntity(): RouteLocation = RouteLocationMapper.toEntity(this)
fun RouteLocation.toDto(): RouteLocationDto = RouteLocationMapper.toDto(this)

/**
 * Route DTO ↔ Entity 轉換
 */
object RouteMapper {
    fun toEntity(dto: RouteDto): Route = Route(
        id = dto.id,
        title = dto.title,
        locations = dto.locations.map { it.toEntity() },
        createdFrom = dto.createdFrom
    )

    fun toDto(entity: Route): RouteDto = RouteDto(
        id = entity.id,
        title = entity.title,
        locations = entity.locations.map { it.toDto() },
        createdFrom = entity.createdFrom
    )
}

fun RouteDto.toEntity(): Route = RouteMapper.toEntity(this)
fun Route.toDto(): RouteDto = RouteMapper.toDto(this)
