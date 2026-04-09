package com.example.myapplication.feature.travel_history.di

import com.example.myapplication.feature.travel_history.domain.usecase.FilterHistoryByDateRangeUseCase
import com.example.myapplication.feature.travel_history.domain.usecase.GetTravelHistoryUseCase
import com.example.myapplication.feature.travel_history.presentation.TravelHistoryViewModel
import kotlin.time.ExperimentalTime
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

@ExperimentalTime
val travelHistoryModule = module {
    // Use Cases
    factory { GetTravelHistoryUseCase(get(), get()) }
    factory { FilterHistoryByDateRangeUseCase(get()) }

    // ViewModels
    viewModel {
        TravelHistoryViewModel(
            getTravelHistoryUseCase = get(),
            filterHistoryByDateRangeUseCase = get()
        )
    }
}
