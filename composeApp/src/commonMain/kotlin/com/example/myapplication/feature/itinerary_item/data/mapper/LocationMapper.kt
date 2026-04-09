package com.example.myapplication.feature.itinerary_item.data.mapper

import com.example.myapplication.feature.itinerary_item.data.dto.LocationDto
import com.example.myapplication.feature.itinerary_item.domain.model.Location

/**
 * Location DTO ??Entity è½‰æ?
 */
object LocationMapper {
    fun toDomain(dto: LocationDto): Location = Location(
        name = dto.name,
        latitude = dto.latitude,
        longitude = dto.longitude,
        address = dto.address
    )

    fun toDto(entity: Location): LocationDto = LocationDto(
        name = entity.name,
        latitude = entity.latitude,
        longitude = entity.longitude,
        address = entity.address
    )
}

fun LocationDto.toDomain(): Location = LocationMapper.toDomain(this)
fun Location.toDto(): LocationDto = LocationMapper.toDto(this)
