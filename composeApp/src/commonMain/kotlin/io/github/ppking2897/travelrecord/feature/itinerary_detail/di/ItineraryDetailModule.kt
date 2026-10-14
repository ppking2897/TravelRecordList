package io.github.ppking2897.travelrecord.feature.itinerary_detail.di

import io.github.ppking2897.travelrecord.feature.itinerary_detail.presentation.ItineraryDetailViewModel
import kotlin.time.ExperimentalTime
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@ExperimentalTime
val itineraryDetailModule = module {
    viewModel {
        ItineraryDetailViewModel(
            getItineraryUseCase = get(),
            getItemsByItineraryUseCase = get(),
            deleteItineraryUseCase = get(),
            createRouteUseCase = get(),
            updateItemUseCase = get(),
            deleteItemUseCase = get(),
            groupItemsByDateUseCase = get(),
            filterItemsByDateUseCase = get(),
            filterByHashtagUseCase = get(),
            addPhotoUseCase = get(),
            deletePhotoUseCase = get(),
            setCoverPhotoUseCase = get(),
            generateThumbnailUseCase = get(),
            batchDeleteItemsUseCase = get(),
            batchUpdateItemsUseCase = get()
        )
    }
}
