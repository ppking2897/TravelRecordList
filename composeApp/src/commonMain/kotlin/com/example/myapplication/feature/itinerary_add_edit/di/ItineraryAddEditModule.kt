package com.example.myapplication.feature.itinerary_add_edit.di

import com.example.myapplication.feature.itinerary_add_edit.presentation.ItineraryAddEditViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val itineraryAddEditModule = module {
    viewModel {
        ItineraryAddEditViewModel(
            createItineraryUseCase = get(),
            updateItineraryUseCase = get(),
            getItineraryUseCase = get(),
            saveDraftUseCase = get(),
            loadDraftUseCase = get(),
            deleteDraftUseCase = get(),
            imageStorageService = get()
        )
    }
}
