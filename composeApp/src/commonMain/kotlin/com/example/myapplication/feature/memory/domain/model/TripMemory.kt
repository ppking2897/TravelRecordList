@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.feature.memory.domain.model

import com.example.myapplication.feature.itinerary.domain.model.Itinerary

data class TripMemory(
    val itinerary: Itinerary,
    val story: MemoryStory,
    val placeCount: Int,
    val photoCount: Int,
    val completedCount: Int,
    val coverPhotoPath: String?
)
