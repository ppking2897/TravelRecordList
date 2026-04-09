package com.example.myapplication.feature.itinerary_item.data.mapper

import com.example.myapplication.feature.itinerary_item.data.model.LocationResponse
import com.example.myapplication.feature.itinerary_item.domain.model.Location

/**
 * Location Response ↔ Domain 轉換
 */
object LocationMapper {
    fun toDomain(dto: LocationResponse): Location = Location(
        name = dto.name,
        latitude = dto.latitude,
        longitude = dto.longitude,
        address = dto.address
    )

    fun toDto(entity: Location): LocationResponse = LocationResponse(
        name = entity.name,
        latitude = entity.latitude,
        longitude = entity.longitude,
        address = entity.address
    )
}

fun LocationResponse.toDomain(): Location = LocationMapper.toDomain(this)
fun Location.toDto(): LocationResponse = LocationMapper.toDto(this)
