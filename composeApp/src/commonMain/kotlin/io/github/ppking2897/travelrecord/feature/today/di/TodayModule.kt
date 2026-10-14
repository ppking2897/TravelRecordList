package io.github.ppking2897.travelrecord.feature.today.di

import io.github.ppking2897.travelrecord.feature.today.presentation.TodayViewModel
import kotlin.time.ExperimentalTime
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@ExperimentalTime
val todayModule = module {
    viewModelOf(::TodayViewModel)
}
