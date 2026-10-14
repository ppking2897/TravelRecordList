@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.footprint.di

import io.github.ppking2897.travelrecord.feature.footprint.domain.usecase.GetVisitedPlacesUseCase
import io.github.ppking2897.travelrecord.feature.footprint.presentation.FootprintViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val footprintModule = module {
    factory { GetVisitedPlacesUseCase(get(), get()) }

    viewModelOf(::FootprintViewModel)
}
