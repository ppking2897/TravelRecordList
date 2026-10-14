package com.example.myapplication.feature.itinerary_list.di

import com.example.myapplication.feature.itinerary_list.presentation.ItineraryListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val itineraryListModule = module {
    viewModel {
        ItineraryListViewModel(
            getAllItinerariesUseCase = get(),
            searchUseCase = get(),
            getItemsByItineraryUseCase = get(),
            initializeSampleDataUseCase = get(),
            deleteItineraryUseCase = get()
        )
    }
}
