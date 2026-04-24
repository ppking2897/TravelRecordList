@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.feature.memory.domain.usecase

import com.example.myapplication.feature.itinerary.domain.model.TravelPhase
import com.example.myapplication.feature.itinerary.domain.usecase.GetAllItinerariesUseCase
import com.example.myapplication.feature.itinerary.domain.usecase.GetItineraryPhaseUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase
import com.example.myapplication.feature.memory.domain.model.TripMemory
import com.example.myapplication.feature.memory.domain.service.MemoryStoryGenerator
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class GetMemoriesUseCase(
    private val getAllItinerariesUseCase: GetAllItinerariesUseCase,
    private val getItemsByItineraryUseCase: GetItemsByItineraryUseCase,
    private val memoryStoryGenerator: MemoryStoryGenerator
) {
    private val phaseUseCase = GetItineraryPhaseUseCase()

    suspend operator fun invoke(): Result<List<TripMemory>> = runCatching {
        val today = currentLocalDate()
        val itineraries = getAllItinerariesUseCase().getOrThrow()

        val completed = itineraries
            .filter { phaseUseCase(it, today) == TravelPhase.Completed }
            .sortedByDescending { it.endDate }

        completed.map { itinerary ->
            val items = getItemsByItineraryUseCase(itinerary.id).getOrElse { emptyList() }
            val story = memoryStoryGenerator.generate(itinerary, items)
            val coverPhotoPath = itinerary.coverPhotoPath
                ?: items.asSequence()
                    .flatMap { it.photos.asSequence() }
                    .firstOrNull()
                    ?.filePath
            TripMemory(
                itinerary = itinerary,
                story = story,
                placeCount = items.size,
                photoCount = items.sumOf { it.photos.size },
                completedCount = items.count { it.isCompleted },
                coverPhotoPath = coverPhotoPath
            )
        }
    }

    private fun currentLocalDate(): LocalDate {
        return Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }
}
