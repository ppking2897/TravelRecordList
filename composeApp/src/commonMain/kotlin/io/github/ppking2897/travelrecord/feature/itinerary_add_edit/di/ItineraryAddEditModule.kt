package io.github.ppking2897.travelrecord.feature.itinerary_add_edit.di

import io.github.ppking2897.travelrecord.feature.itinerary_add_edit.presentation.ItineraryAddEditViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val itineraryAddEditModule = module {
    viewModelOf(::ItineraryAddEditViewModel)
}
