package io.github.ppking2897.travelrecord.feature.itinerary_map.di

import io.github.ppking2897.travelrecord.feature.itinerary_map.presentation.ItineraryMapViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val itineraryMapModule = module {
    viewModel {
        ItineraryMapViewModel(
            getItineraryUseCase = get(),
            getMapMarkersUseCase = get()
        )
    }
}
