package com.example.myapplication.data.mapper

import com.example.myapplication.data.dto.ItineraryItemDto
import com.example.myapplication.domain.entity.ItineraryItem

/**
 * ItineraryItem DTO ↔ Entity 轉換
 */
object ItineraryItemMapper {
    fun toEntity(dto: ItineraryItemDto): ItineraryItem = ItineraryItem(
        id = dto.id,
        itineraryId = dto.itineraryId,
        date = dto.date,
        arrivalTime = dto.arrivalTime,
        departureTime = dto.departureTime,
        location = dto.location.toEntity(),
        activity = dto.activity,
        notes = dto.notes,
        hashtags = dto.hashtags.map { it.toEntity() },
        photos = dto.photos.map { it.toEntity() },
        coverPhotoId = dto.coverPhotoId,
        isCompleted = dto.isCompleted,
        completedAt = dto.completedAt,
        createdAt = dto.createdAt,
        modifiedAt = dto.modifiedAt
    )

    fun toDto(entity: ItineraryItem): ItineraryItemDto = ItineraryItemDto(
        id = entity.id,
        itineraryId = entity.itineraryId,
        date = entity.date,
        arrivalTime = entity.arrivalTime,
        departureTime = entity.departureTime,
        location = entity.location.toDto(),
        activity = entity.activity,
        notes = entity.notes,
        hashtags = entity.hashtags.map { it.toDto() },
        photos = entity.photos.map { it.toDto() },
        coverPhotoId = entity.coverPhotoId,
        isCompleted = entity.isCompleted,
        completedAt = entity.completedAt,
        createdAt = entity.createdAt,
        modifiedAt = entity.modifiedAt
    )
}

fun ItineraryItemDto.toEntity(): ItineraryItem = ItineraryItemMapper.toEntity(this)
fun ItineraryItem.toDto(): ItineraryItemDto = ItineraryItemMapper.toDto(this)
