@file:OptIn(ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.itinerary.data.local

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import kotlinx.datetime.LocalDate
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 行程的 items 不存在這張表，寫入時忽略 */
fun Itinerary.toEntity() = ItineraryEntity(
    id = id,
    title = title,
    description = description,
    startDate = startDate?.toEpochDays(),
    endDate = endDate?.toEpochDays(),
    coverPhotoPath = coverPhotoPath,
    createdAt = createdAt.toEpochMilliseconds(),
    modifiedAt = modifiedAt.toEpochMilliseconds(),
)

fun ItineraryEntity.toDomain(items: List<ItineraryItem>) = Itinerary(
    id = id,
    title = title,
    description = description,
    startDate = startDate?.let { LocalDate.fromEpochDays(it) },
    endDate = endDate?.let { LocalDate.fromEpochDays(it) },
    coverPhotoPath = coverPhotoPath,
    items = items,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    modifiedAt = Instant.fromEpochMilliseconds(modifiedAt),
)
