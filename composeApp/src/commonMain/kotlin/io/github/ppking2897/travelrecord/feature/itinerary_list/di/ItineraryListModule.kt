package io.github.ppking2897.travelrecord.feature.itinerary_list.di

import io.github.ppking2897.travelrecord.feature.itinerary_list.presentation.ItineraryListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val itineraryListModule = module {
    viewModelOf(::ItineraryListViewModel)
}
