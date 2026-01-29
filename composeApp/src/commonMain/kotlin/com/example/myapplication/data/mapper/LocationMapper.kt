package com.example.myapplication.data.mapper

import com.example.myapplication.data.dto.LocationDto
import com.example.myapplication.domain.entity.Location

/**
 * Location DTO ↔ Entity 轉換
 */
object LocationMapper {
    fun toEntity(dto: LocationDto): Location = Location(
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

fun LocationDto.toEntity(): Location = LocationMapper.toEntity(this)
fun Location.toDto(): LocationDto = LocationMapper.toDto(this)
