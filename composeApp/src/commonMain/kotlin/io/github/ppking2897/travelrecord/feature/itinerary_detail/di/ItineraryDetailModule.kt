package io.github.ppking2897.travelrecord.feature.itinerary_detail.di

import io.github.ppking2897.travelrecord.feature.itinerary_detail.presentation.ItineraryDetailViewModel
import kotlin.time.ExperimentalTime
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@ExperimentalTime
val itineraryDetailModule = module {
    viewModelOf(::ItineraryDetailViewModel)
}
