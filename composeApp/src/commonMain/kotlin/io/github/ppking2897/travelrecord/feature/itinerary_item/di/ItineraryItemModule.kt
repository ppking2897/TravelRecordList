package io.github.ppking2897.travelrecord.feature.itinerary_item.di

import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.HashtagRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.ItineraryItemRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary_item.data.repository.PhotoRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.HashtagRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.PhotoRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.AddPhotoUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.BatchDeleteItemsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.BatchUpdateItemsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.DeleteItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.DeletePhotoUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.ExtractHashtagsUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.FilterByHashtagUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.FilterItemsByDateUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GenerateThumbnailUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GetItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GetItemsByItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GroupItemsByDateUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.ReorderPhotosUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.SetCoverPhotoUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.UpdateItineraryItemUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.presentation.AddEditItemViewModel
import io.github.ppking2897.travelrecord.feature.itinerary_item.presentation.EditItemViewModel
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
