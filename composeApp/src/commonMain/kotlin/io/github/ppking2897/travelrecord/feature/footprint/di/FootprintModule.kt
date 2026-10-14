@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.feature.footprint.di

import com.example.myapplication.feature.footprint.domain.usecase.GetVisitedPlacesUseCase
import com.example.myapplication.feature.footprint.presentation.FootprintViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val footprintModule = module {
    factory { GetVisitedPlacesUseCase(get(), get()) }

    viewModel {
        FootprintViewModel(
            getVisitedPlacesUseCase = get()
        )
    }
}
