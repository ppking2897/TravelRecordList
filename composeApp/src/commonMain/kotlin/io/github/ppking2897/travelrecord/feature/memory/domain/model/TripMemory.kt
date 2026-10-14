@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.memory.domain.model

import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary

data class TripMemory(
    val itinerary: Itinerary,
    val story: MemoryStory,
    val placeCount: Int,
    val photoCount: Int,
    val completedCount: Int,
    val coverPhotoPath: String?
)
