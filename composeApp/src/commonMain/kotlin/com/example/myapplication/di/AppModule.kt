package com.example.myapplication.di

import com.example.myapplication.data.repository.*
import com.example.myapplication.data.storage.InMemoryStorageService
import com.example.myapplication.data.storage.StorageService
import com.example.myapplication.data.sync.SyncManager
import com.example.myapplication.domain.usecase.*
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
    
    // Use Cases
    factory { CreateItineraryUseCase(get()) }
    factory { AddItineraryItemUseCase(get(), get()) }
    factory { UpdateItineraryItemUseCase(get()) }
    factory { DeleteItineraryItemUseCase(get()) }
    factory { GetTravelHistoryUseCase(get(), get()) }
    factory { CreateRouteFromItineraryUseCase(get()) }
    factory { SearchItinerariesUseCase(get()) }
    factory { AddPhotoToItemUseCase(get()) }
    factory { RemovePhotoFromItemUseCase(get()) }
    factory { GroupItemsByDateUseCase() }
    factory { FilterItemsByDateUseCase() }
    
    // ViewModels
    viewModel { ItineraryListViewModel(get(), get()) }
    viewModel { ItineraryDetailViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { TravelHistoryViewModel(get(), get()) }
}
