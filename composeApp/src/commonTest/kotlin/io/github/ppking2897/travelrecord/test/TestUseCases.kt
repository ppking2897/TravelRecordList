package io.github.ppking2897.travelrecord.test

import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetCurrentTripUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.HashtagRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.PhotoRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddPhotoUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.ExtractHashtagsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GenerateThumbnailUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.UpdateItineraryItemUseCase

/**
 * 照 App 的接法，把測試用的 Repository 組成 use case
 *
 * ViewModel 測試只從這裡拿 use case，不直接碰資料層的實作。
 */
class TestUseCases(
    val itineraries: ItineraryRepository,
    val items: ItineraryItemRepository,
    val imageStorage: ImageStorageService = FakeImageStorageService(),
) {
    private val extractHashtags = ExtractHashtagsUseCase(HashtagRepositoryImpl())
    private val photos = PhotoRepositoryImpl(imageStorage, itineraries, items)

    val getCurrentTrip = GetCurrentTripUseCase(itineraries)
    val getItemsByItinerary = GetItemsByItineraryUseCase(items)
    val addItem = AddItineraryItemUseCase(items, itineraries, extractHashtags)
    val updateItem = UpdateItineraryItemUseCase(items, itineraries, extractHashtags)
    val addPhoto = AddPhotoUseCase(photos)
    val generateThumbnail = GenerateThumbnailUseCase(photos)
}
