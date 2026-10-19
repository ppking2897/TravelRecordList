@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.ppking2897.travelrecord.test

import io.github.ppking2897.travelrecord.core.data.local.InMemoryStorageService
import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.core.domain.service.LegacyDataImporter
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.DeleteItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.EnsureLocalDataReadyUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetAllItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetCurrentTripUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.InitializeSampleDataUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.SearchItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.HashtagRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.PhotoRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddPhotoUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.BatchDeleteItemsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.BatchUpdateItemsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.DeleteItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.DeletePhotoUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.ExtractHashtagsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GenerateThumbnailUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.SetCoverPhotoUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.UpdateItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.route.data.repository.RouteRepositoryImpl
import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository
import io.github.ppking2897.travelrecord.feature.route.domain.usecase.CreateRouteFromItineraryUseCase

/**
 * 照 App 的接法，把測試用的 Repository 組成 use case
 *
 * ViewModel 測試只從這裡拿 use case，不直接碰資料層的實作。
 */
class TestUseCases(
    val itineraries: ItineraryRepository,
    val items: ItineraryItemRepository,
    val imageStorage: ImageStorageService = FakeImageStorageService(),
    legacyDataImporter: LegacyDataImporter = FakeLegacyDataImporter(Result.success(Unit)),
) {
    private val extractHashtags = ExtractHashtagsUseCase(HashtagRepositoryImpl())
    private val photos = PhotoRepositoryImpl(imageStorage, itineraries, items)

    /** 路線存在記憶體，測試可以讀回剛生成的路線 */
    val routes: RouteRepository = RouteRepositoryImpl(InMemoryStorageService(), itineraries)

    val getCurrentTrip = GetCurrentTripUseCase(itineraries)
    val getAllItineraries = GetAllItinerariesUseCase(itineraries)
    val getItinerary = GetItineraryUseCase(itineraries)
    val searchItineraries = SearchItinerariesUseCase(itineraries)
    val deleteItinerary = DeleteItineraryUseCase(itineraries, items, imageStorage)
    val ensureLocalDataReady = EnsureLocalDataReadyUseCase(legacyDataImporter, InitializeSampleDataUseCase(itineraries, items))

    val getItemsByItinerary = GetItemsByItineraryUseCase(items)
    val addItem = AddItineraryItemUseCase(items, itineraries, extractHashtags)
    val updateItem = UpdateItineraryItemUseCase(items, itineraries, extractHashtags)
    val deleteItem = DeleteItineraryItemUseCase(items, itineraries, imageStorage)
    val batchDeleteItems = BatchDeleteItemsUseCase(items, imageStorage)
    val batchUpdateItems = BatchUpdateItemsUseCase(items)

    val addPhoto = AddPhotoUseCase(photos)
    val deletePhoto = DeletePhotoUseCase(photos)
    val setCoverPhoto = SetCoverPhotoUseCase(photos)
    val generateThumbnail = GenerateThumbnailUseCase(photos)

    val createRoute = CreateRouteFromItineraryUseCase(routes)
}

/** 舊資料搬移一律回傳指定的結果 */
class FakeLegacyDataImporter(private val result: Result<Unit>) : LegacyDataImporter {
    override suspend fun importIfNeeded(): Result<Unit> = result
}
