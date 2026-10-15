@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.itinerary_item.data.local

import io.github.ppking2897.travelrecord.core.data.local.InstantSerializer
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper.toDomain
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.mapper.toDto
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.model.HashtagResponse
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.model.PhotoResponse
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Location
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 照片與標籤沿用原本的 JSON 格式（PhotoResponse / HashtagResponse），時間點保留完整精度。
 * 列上的時間點存 epoch 毫秒；當天時間存奈秒數，可精確還原也能直接排序。
 */
private val listJson = Json {
    ignoreUnknownKeys = true
    serializersModule = SerializersModule { contextual(InstantSerializer) }
}
private val photoListSerializer = ListSerializer(PhotoResponse.serializer())
private val hashtagListSerializer = ListSerializer(HashtagResponse.serializer())

fun ItineraryItem.toEntity() = ItineraryItemEntity(
    id = id,
    itineraryId = itineraryId,
    date = date.toEpochDays(),
    arrivalTime = arrivalTime?.toNanosecondOfDay(),
    departureTime = departureTime?.toNanosecondOfDay(),
    locationName = location.name,
    locationLatitude = location.latitude,
    locationLongitude = location.longitude,
    locationAddress = location.address,
    activity = activity,
    notes = notes,
    hashtagsJson = listJson.encodeToString(hashtagListSerializer, hashtags.map { it.toDto() }),
    photosJson = listJson.encodeToString(photoListSerializer, photos.map { it.toDto() }),
    coverPhotoId = coverPhotoId,
    isCompleted = isCompleted,
    completedAt = completedAt?.toEpochMilliseconds(),
    sortOrder = order,
    createdAt = createdAt.toEpochMilliseconds(),
    modifiedAt = modifiedAt.toEpochMilliseconds(),
)

fun ItineraryItemEntity.toDomain() = ItineraryItem(
    id = id,
    itineraryId = itineraryId,
    date = LocalDate.fromEpochDays(date),
    arrivalTime = arrivalTime?.let { LocalTime.fromNanosecondOfDay(it) },
    departureTime = departureTime?.let { LocalTime.fromNanosecondOfDay(it) },
    location = Location(
        name = locationName,
        latitude = locationLatitude,
        longitude = locationLongitude,
        address = locationAddress,
    ),
    activity = activity,
    notes = notes,
    hashtags = listJson.decodeFromString(hashtagListSerializer, hashtagsJson).map { it.toDomain() },
    photos = listJson.decodeFromString(photoListSerializer, photosJson).map { it.toDomain() },
    coverPhotoId = coverPhotoId,
    isCompleted = isCompleted,
    completedAt = completedAt?.let { Instant.fromEpochMilliseconds(it) },
    order = sortOrder,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    modifiedAt = Instant.fromEpochMilliseconds(modifiedAt),
)
