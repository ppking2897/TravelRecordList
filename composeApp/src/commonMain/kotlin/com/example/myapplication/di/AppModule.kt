package com.example.myapplication.di

import com.example.myapplication.data.repository.*
import com.example.myapplication.data.service.GooglePlacesService
import com.example.myapplication.domain.repository.*
import com.example.myapplication.domain.service.LocationSearchService
import com.example.myapplication.data.sync.SyncManager
import com.example.myapplication.domain.interactor.ItemInteractor
import com.example.myapplication.domain.interactor.PhotoInteractor
import com.example.myapplication.domain.usecase.*
import com.example.myapplication.presentation.add_edit_itinerary.AddEditItineraryViewModel
import com.example.myapplication.presentation.add_edit_item.AddEditItemViewModel
import com.example.myapplication.presentation.itinerary_detail.ItineraryDetailViewModel as ItineraryDetailViewModelMVI
import com.example.myapplication.presentation.edit_item.EditItemViewModel
import com.example.myapplication.presentation.travel_history.TravelHistoryViewModel as TravelHistoryViewModelMVI
import com.example.myapplication.presentation.itinerary_list.ItineraryListViewModel as ItineraryListViewModelMVI
import com.example.myapplication.presentation.route_view.RouteViewViewModel
import com.example.myapplication.presentation.itinerary_map.ItineraryMapViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin 依賴注入模組（通用模組）
 *
 * StorageService 由各平台模組提供：
 * - Android: androidPlatformModule (使用 DataStore)
 * - iOS: iosPlatformModule (使用 UserDefaults)
 * - Web: jsPlatformModule (使用 LocalStorage)
 */
@ExperimentalTime
val appModule = module {
    // HTTP Client for API calls
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    // Location Search Service (Google Places API)
    single<LocationSearchService> { GooglePlacesService(get()) }

    // Sync
    single { SyncManager(get()) }

    // Repositories
    single<ItineraryRepository> { ItineraryRepositoryImpl(get()) }
    single<ItineraryItemRepository> { ItineraryItemRepositoryImpl(get()) }
    single<RouteRepository> { RouteRepositoryImpl(get(), get()) }
    single<DraftRepository> { DraftRepositoryImpl(get()) }
    single<PhotoRepository> { PhotoRepositoryImpl(get(), get(), get()) }
    single<HashtagRepository> { HashtagRepositoryImpl(get()) }

    // Use Cases
    factory { CreateItineraryUseCase(get()) }
    factory { UpdateItineraryUseCase(get()) }
    factory { DeleteItineraryUseCase(get(), get()) }
    factory { ExtractHashtagsUseCase(get()) }
    factory { AddItineraryItemUseCase(get(), get(), get()) }
    factory { UpdateItineraryItemUseCase(get(), get(), get()) }
    factory { DeleteItineraryItemUseCase(get(), get()) }
    factory { GetTravelHistoryUseCase(get(), get()) }
    factory { CreateRouteFromItineraryUseCase(get()) }
    factory { SearchItinerariesUseCase(get()) }
    factory { AddPhotoUseCase(get()) }
    factory { GenerateThumbnailUseCase(get()) }
    factory { DeletePhotoUseCase(get()) }
    factory { SetCoverPhotoUseCase(get()) }
    factory { ReorderPhotosUseCase(get()) }
    factory { FilterByHashtagUseCase(get()) }
    factory { GroupItemsByDateUseCase() }
    factory { FilterItemsByDateUseCase() }
    factory { SaveDraftUseCase(get()) }
    factory { LoadDraftUseCase(get()) }
    // 批量操作 Use Cases
    factory { BatchDeleteItemsUseCase(get()) }
    factory { BatchUpdateItemsUseCase(get()) }

    // 地圖 Use Cases
    factory { GetMapMarkersUseCase(get()) }

    // Interactors
    factory {
        ItemInteractor(
            itemRepository = get(),
            updateItemUseCase = get(),
            deleteItemUseCase = get(),
            groupItemsByDateUseCase = get(),
            filterItemsByDateUseCase = get(),
            filterByHashtagUseCase = get()
        )
    }
    factory {
        PhotoInteractor(
            addPhotoUseCase = get(),
            deletePhotoUseCase = get(),
            setCoverPhotoUseCase = get(),
            generateThumbnailUseCase = get()
        )
    }

    // MVI ViewModels
    viewModel { RouteViewViewModel(get()) }
    viewModel { TravelHistoryViewModelMVI(get(), get()) }
    viewModel { ItineraryListViewModelMVI(get(), get()) }
    viewModel {
        ItineraryDetailViewModelMVI(
            itineraryRepository = get(),
            deleteItineraryUseCase = get(),
            createRouteUseCase = get(),
            itemInteractor = get(),
            photoInteractor = get(),
            batchDeleteItemsUseCase = get(),
            batchUpdateItemsUseCase = get()
        )
    }
    viewModel { AddEditItineraryViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AddEditItemViewModel(get(), get(), get()) }
    viewModel { EditItemViewModel(get(), get(), get(), get()) }
    viewModel { ItineraryMapViewModel(get(), get()) }
}
