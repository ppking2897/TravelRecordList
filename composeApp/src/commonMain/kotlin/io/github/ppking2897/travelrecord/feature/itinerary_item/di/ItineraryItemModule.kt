package com.example.myapplication.feature.itinerary_item.di

import com.example.myapplication.feature.itinerary_item.data.repository.HashtagRepositoryImpl
import com.example.myapplication.feature.itinerary_item.data.repository.ItineraryItemRepositoryImpl
import com.example.myapplication.feature.itinerary_item.data.repository.PhotoRepositoryImpl
import com.example.myapplication.feature.itinerary_item.domain.repository.HashtagRepository
import com.example.myapplication.feature.itinerary_item.domain.repository.ItineraryItemRepository
import com.example.myapplication.feature.itinerary_item.domain.repository.PhotoRepository
import com.example.myapplication.feature.itinerary_item.domain.usecase.AddItineraryItemUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.AddPhotoUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.BatchDeleteItemsUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.BatchUpdateItemsUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.DeleteItineraryItemUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.DeletePhotoUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.ExtractHashtagsUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.FilterByHashtagUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.FilterItemsByDateUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.GenerateThumbnailUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.GetItineraryItemUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.GroupItemsByDateUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.ReorderPhotosUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.SetCoverPhotoUseCase
import com.example.myapplication.feature.itinerary_item.domain.usecase.UpdateItineraryItemUseCase
import com.example.myapplication.feature.itinerary_item.presentation.AddEditItemViewModel
import com.example.myapplication.feature.itinerary_item.presentation.EditItemViewModel
import kotlin.time.ExperimentalTime
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@ExperimentalTime
val itineraryItemModule = module {
    // Repositories
    single<ItineraryItemRepository> { ItineraryItemRepositoryImpl(get()) }
    single<PhotoRepository> { PhotoRepositoryImpl(get(), get(), get()) }
    single<HashtagRepository> { HashtagRepositoryImpl(get()) }

    // Use Cases
    factory { GetItemsByItineraryUseCase(get()) }
    factory { GetItineraryItemUseCase(get()) }
    factory { ExtractHashtagsUseCase(get()) }
    factory { AddItineraryItemUseCase(get(), get(), get()) }
    factory { UpdateItineraryItemUseCase(get(), get(), get()) }
    factory { DeleteItineraryItemUseCase(get(), get()) }
    factory { AddPhotoUseCase(get()) }
    factory { GenerateThumbnailUseCase(get()) }
    factory { DeletePhotoUseCase(get()) }
    factory { SetCoverPhotoUseCase(get()) }
    factory { ReorderPhotosUseCase(get()) }
    factory { FilterByHashtagUseCase(get()) }
    factory { GroupItemsByDateUseCase() }
    factory { FilterItemsByDateUseCase() }
    factory { BatchDeleteItemsUseCase(get()) }
    factory { BatchUpdateItemsUseCase(get()) }

    // ViewModels
    viewModel {
        AddEditItemViewModel(
            addItemUseCase = get(),
            getItineraryUseCase = get(),
            imageStorageService = get()
        )
    }
    viewModel {
        EditItemViewModel(
            updateItemUseCase = get(),
            getItineraryItemUseCase = get(),
            getItineraryUseCase = get(),
            imageStorageService = get()
        )
    }
}
