package io.github.ppking2897.travelrecord.feature.route.di

import io.github.ppking2897.travelrecord.feature.route.data.repository.RouteRepositoryImpl
import io.github.ppking2897.travelrecord.feature.route.domain.repository.RouteRepository
import io.github.ppking2897.travelrecord.feature.route.domain.usecase.CreateRouteFromItineraryUseCase
import io.github.ppking2897.travelrecord.feature.route.domain.usecase.ExportRouteUseCase
import io.github.ppking2897.travelrecord.feature.route.domain.usecase.GetMapMarkersUseCase
import io.github.ppking2897.travelrecord.feature.route.domain.usecase.GetRouteUseCase
import io.github.ppking2897.travelrecord.feature.route.presentation.RouteViewViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val routeModule = module {
    // Repository
    single<RouteRepository> { RouteRepositoryImpl(get(), get()) }

    // Use Cases
    factory { CreateRouteFromItineraryUseCase(get()) }
    factory { GetMapMarkersUseCase(get()) }
    factory { GetRouteUseCase(get()) }
    factory { ExportRouteUseCase(get()) }

    // ViewModels
    viewModelOf(::RouteViewViewModel)
}
