package io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper

import io.github.ppking2897.travelrecord.feature.itinerary_item.data.model.ItineraryItemResponse
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem

/**
 * ItineraryItem Response ↔ Domain 轉換
 */
object ItineraryItemMapper {
    fun toDomain(dto: ItineraryItemResponse): ItineraryItem = ItineraryItem(
        id = dto.id,
        itineraryId = dto.itineraryId,
        date = dto.date,
        arrivalTime = dto.arrivalTime,
        departureTime = dto.departureTime,
        location = dto.location.toDomain(),
        activity = dto.activity,
        notes = dto.notes,
        hashtags = dto.hashtags.map { it.toDomain() },
        photos = dto.photos.map { it.toDomain() },
        coverPhotoId = dto.coverPhotoId,
        isCompleted = dto.isCompleted,
        completedAt = dto.completedAt,
        order = dto.order,
        createdAt = dto.createdAt,
        modifiedAt = dto.modifiedAt
    )

    fun toDto(entity: ItineraryItem): ItineraryItemResponse = ItineraryItemResponse(
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
        order = entity.order,
        createdAt = entity.createdAt,
        modifiedAt = entity.modifiedAt
    )
}

fun ItineraryItemResponse.toDomain(): ItineraryItem = ItineraryItemMapper.toDomain(this)
fun ItineraryItem.toDto(): ItineraryItemResponse = ItineraryItemMapper.toDto(this)
