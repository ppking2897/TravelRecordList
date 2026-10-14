package io.github.ppking2897.travelrecord.feature.itinerary.data.mapper

import io.github.ppking2897.travelrecord.feature.itinerary.data.model.ItineraryResponse
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper.toDto
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper.toDomain

/**
 * Itinerary Response ↔ Domain 轉換
 */
object ItineraryMapper {
    fun toDomain(dto: ItineraryResponse): Itinerary = Itinerary(
        id = dto.id,
        title = dto.title,
        description = dto.description,
        startDate = dto.startDate,
        endDate = dto.endDate,
        coverPhotoPath = dto.coverPhotoPath,
        items = dto.items.map { it.toDomain() },
        createdAt = dto.createdAt,
        modifiedAt = dto.modifiedAt
    )

    fun toDto(entity: Itinerary): ItineraryResponse = ItineraryResponse(
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

fun ItineraryResponse.toDomain(): Itinerary = ItineraryMapper.toDomain(this)
fun Itinerary.toDto(): ItineraryResponse = ItineraryMapper.toDto(this)
