@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.ppking2897.travelrecord.test

import io.github.ppking2897.travelrecord.core.data.local.InMemoryStorageService
import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.core.domain.service.LegacyDataImporter
import io.github.ppking2897.travelrecord.feature.draft.data.repository.DraftRepositoryImpl
import io.github.ppking2897.travelrecord.feature.draft.domain.repository.DraftRepository
import io.github.ppking2897.travelrecord.feature.draft.domain.usecase.DeleteDraftUseCase
import io.github.ppking2897.travelrecord.feature.draft.domain.usecase.LoadDraftUseCase
import io.github.ppking2897.travelrecord.feature.draft.domain.usecase.SaveDraftUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.SampleDataRecordRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.SampleDataRecordRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.CreateItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.DeleteItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.EnsureLocalDataReadyUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetAllItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetCurrentTripUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.InitializeSampleDataUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.SearchItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.UpdateItineraryUseCase
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
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GetItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.SetCoverPhotoUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.UpdateItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.route.data.repository.RouteRepositoryImpl
import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository
import io.github.ppking2897.travelrecord.feature.route.domain.usecase.ExportRouteUseCase
import io.github.ppking2897.travelrecord.feature.route.domain.usecase.GetRouteForItineraryUseCase

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
    /** 草稿存在記憶體；同一個 [TestUseCases] 建立的 ViewModel 讀得到彼此存的草稿 */
    drafts: DraftRepository = inMemoryDraftRepository(),
) {
    private val extractHashtags = ExtractHashtagsUseCase(HashtagRepositoryImpl())
    private val photos = PhotoRepositoryImpl(imageStorage, itineraries, items)

    /** 路線依行程現算；清舊版路線用的鍵值儲存換成記憶體 */
    private val routes: RouteRepository = RouteRepositoryImpl(InMemoryStorageService(), itineraries)

    val getCurrentTrip = GetCurrentTripUseCase(itineraries)
    val getAllItineraries = GetAllItinerariesUseCase(itineraries)
    val getItinerary = GetItineraryUseCase(itineraries)
    val createItinerary = CreateItineraryUseCase(itineraries)
    val updateItinerary = UpdateItineraryUseCase(itineraries)
    val searchItineraries = SearchItinerariesUseCase(itineraries)
    val deleteItinerary = DeleteItineraryUseCase(itineraries, items, imageStorage)
    val ensureLocalDataReady = EnsureLocalDataReadyUseCase(
        legacyDataImporter,
        InitializeSampleDataUseCase(itineraries, items, inMemorySampleDataRecord()),
        routes,
    )

    val getItemsByItinerary = GetItemsByItineraryUseCase(items)
    val getItem = GetItineraryItemUseCase(items)
    val addItem = AddItineraryItemUseCase(items, itineraries, extractHashtags)
    val updateItem = UpdateItineraryItemUseCase(items, itineraries, extractHashtags)
    val deleteItem = DeleteItineraryItemUseCase(items, itineraries, imageStorage)
    val batchDeleteItems = BatchDeleteItemsUseCase(items, imageStorage)
    val batchUpdateItems = BatchUpdateItemsUseCase(items)

    val addPhoto = AddPhotoUseCase(photos)
    val deletePhoto = DeletePhotoUseCase(photos)
    val setCoverPhoto = SetCoverPhotoUseCase(photos)
    val generateThumbnail = GenerateThumbnailUseCase(photos)

    val getRouteForItinerary = GetRouteForItineraryUseCase(routes)
    val exportRoute = ExportRouteUseCase(routes)

    val saveDraft = SaveDraftUseCase(drafts)
    val loadDraft = LoadDraftUseCase(drafts)
    val deleteDraft = DeleteDraftUseCase(drafts)
}

/** App 用的草稿 Repository，底層換成記憶體儲存 */
fun inMemoryDraftRepository(): DraftRepository = DraftRepositoryImpl(InMemoryStorageService())

/** App 用的「範例已放過」紀錄，底層換成記憶體儲存 */
fun inMemorySampleDataRecord(): SampleDataRecordRepository = SampleDataRecordRepositoryImpl(InMemoryStorageService())

/** 舊資料搬移一律回傳指定的結果 */
class FakeLegacyDataImporter(private val result: Result<Unit>) : LegacyDataImporter {
    override suspend fun importIfNeeded(): Result<Unit> = result
}
