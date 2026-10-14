package com.example.myapplication.feature.today.di

import com.example.myapplication.feature.today.presentation.TodayViewModel
import kotlin.time.ExperimentalTime
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@ExperimentalTime
val todayModule = module {
    viewModel {
        TodayViewModel(
            getAllItinerariesUseCase = get(),
            getItemsByItineraryUseCase = get(),
            addItineraryItemUseCase = get(),
            updateItineraryItemUseCase = get(),
            addPhotoUseCase = get(),
            generateThumbnailUseCase = get(),
            imageStorageService = get()
        )
    }
}
