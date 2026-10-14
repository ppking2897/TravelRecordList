package io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper

import io.github.ppking2897.travelrecord.feature.itinerary_item.data.model.PhotoResponse
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Photo

/**
 * Photo Response ↔ Domain 轉換
 */
object PhotoMapper {
    fun toDomain(dto: PhotoResponse): Photo = Photo(
        id = dto.id,
        itemId = dto.itemId,
        fileName = dto.fileName,
        filePath = dto.filePath,
        thumbnailPath = dto.thumbnailPath,
        order = dto.order,
        isCover = dto.isCover,
        width = dto.width,
        height = dto.height,
        fileSize = dto.fileSize,
        uploadedAt = dto.uploadedAt,
        modifiedAt = dto.modifiedAt
    )

    fun toDto(entity: Photo): PhotoResponse = PhotoResponse(
        id = entity.id,
        itemId = entity.itemId,
        fileName = entity.fileName,
        filePath = entity.filePath,
        thumbnailPath = entity.thumbnailPath,
        order = entity.order,
        isCover = entity.isCover,
        width = entity.width,
        height = entity.height,
        fileSize = entity.fileSize,
        uploadedAt = entity.uploadedAt,
        modifiedAt = entity.modifiedAt
    )
}

fun PhotoResponse.toDomain(): Photo = PhotoMapper.toDomain(this)
fun Photo.toDto(): PhotoResponse = PhotoMapper.toDto(this)
