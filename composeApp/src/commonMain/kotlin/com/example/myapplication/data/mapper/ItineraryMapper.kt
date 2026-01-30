package com.example.myapplication.data.mapper

import com.example.myapplication.data.dto.ItineraryDto
import com.example.myapplication.domain.entity.Itinerary

/**
 * Itinerary DTO ↔ Entity 轉換
 */
object ItineraryMapper {
    fun toEntity(dto: ItineraryDto): Itinerary = Itinerary(
        id = dto.id,
        title = dto.title,
        description = dto.description,
        startDate = dto.startDate,
        endDate = dto.endDate,
        coverPhotoPath = dto.coverPhotoPath,
        items = dto.items.map { it.toEntity() },
        createdAt = dto.createdAt,
        modifiedAt = dto.modifiedAt
    )

    fun toDto(entity: Itinerary): ItineraryDto = ItineraryDto(
        id = entity.id,
        title = entity.title,
        description = entity.description,
        startDate = entity.startDate,
        endDate = entity.endDate,
        coverPhotoPath = entity.coverPhotoPath,
        items = entity.items.map { it.toDto() },
        createdAt = entity.createdAt,
        modifiedAt = entity.modifiedAt
    )
}

fun ItineraryDto.toEntity(): Itinerary = ItineraryMapper.toEntity(this)
fun Itinerary.toDto(): ItineraryDto = ItineraryMapper.toDto(this)
