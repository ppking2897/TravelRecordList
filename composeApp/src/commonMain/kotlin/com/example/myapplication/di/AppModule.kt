package com.example.myapplication.di

import com.example.myapplication.data.repository.*
import com.example.myapplication.data.storage.InMemoryStorageService
import com.example.myapplication.data.storage.StorageService
import com.example.myapplication.data.sync.SyncManager
import com.example.myapplication.domain.usecase.*
import com.example.myapplication.ui.mvi.addedit.AddEditItineraryViewModel
import com.example.myapplication.ui.mvi.additem.AddEditItemViewModel
import com.example.myapplication.ui.mvi.detail.ItineraryDetailViewModel as ItineraryDetailViewModelMVI
import com.example.myapplication.ui.mvi.edititem.EditItemViewModel
import com.example.myapplication.ui.mvi.history.TravelHistoryViewModel as TravelHistoryViewModelMVI
import com.example.myapplication.ui.mvi.itinerary.ItineraryListViewModel as ItineraryListViewModelMVI
import com.example.myapplication.ui.mvi.route.RouteViewViewModel
import com.example.myapplication.ui.viewmodel.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

/**
 * Koin 依賴注入模組
 * 
 * 注意：StorageService 需要在各平台實作中提供
 * - Android: 使用 DataStore
 * - iOS: 使用 UserDefaults
 * - Web: 使用 LocalStorage
 */
@ExperimentalTime
val appModule = module {
    // Storage (使用 InMemoryStorageService 用於開發)
    // 生產環境應該替換為平台特定的實作
    single<StorageService> { InMemoryStorageService() }
    
    // Sync
    single { SyncManager(get()) }
    
    // Repositories
    single<ItineraryRepository> { ItineraryRepositoryImpl(get()) }
    single<ItineraryItemRepository> { ItineraryItemRepositoryImpl(get()) }
    single<RouteRepository> { RouteRepositoryImpl(get(), get()) }
    single<DraftRepository> { DraftRepositoryImpl(get()) }
    single<PhotoRepository> { PhotoRepositoryImpl(get(), get()) }
    single<HashtagRepository> { HashtagRepositoryImpl(get()) }
    
    // Use Cases
    factory { CreateItineraryUseCase(get()) }
    factory { UpdateItineraryUseCase(get()) }
    factory { DeleteItineraryUseCase(get(), get()) }
    factory { ExtractHashtagsUseCase(get()) }
    factory { AddItineraryItemUseCase(get(), get(), get()) }
    factory { UpdateItineraryItemUseCase(get(), get()) }
    factory { DeleteItineraryItemUseCase(get()) }
    factory { GetTravelHistoryUseCase(get(), get()) }
    factory { CreateRouteFromItineraryUseCase(get()) }
    factory { SearchItinerariesUseCase(get()) }
    factory { AddPhotoToItemUseCase(get()) }
    factory { RemovePhotoFromItemUseCase(get()) }
    factory { AddPhotoUseCase(get()) }
    factory { DeletePhotoUseCase(get()) }
    factory { SetCoverPhotoUseCase(get()) }
    factory { ReorderPhotosUseCase(get()) }
    factory { FilterByHashtagUseCase(get()) }
    factory { GroupItemsByDateUseCase() }
    factory { FilterItemsByDateUseCase() }
    factory { SaveDraftUseCase(get()) }
    factory { LoadDraftUseCase(get()) }
    
    // ViewModels
    viewModel { ItineraryListViewModel(get(), get()) }
    viewModel { ItineraryDetailViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { TravelHistoryViewModel(get(), get()) }
    
    // MVI ViewModels
    viewModel { RouteViewViewModel(get()) }
    viewModel { TravelHistoryViewModelMVI(get(), get()) }
    viewModel { ItineraryListViewModelMVI(get(), get()) }
    viewModel { ItineraryDetailViewModelMVI(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AddEditItineraryViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { AddEditItemViewModel(get()) }
    viewModel { EditItemViewModel(get()) }
}
