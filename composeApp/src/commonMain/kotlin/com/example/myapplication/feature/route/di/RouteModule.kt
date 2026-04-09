package com.example.myapplication.feature.route.di

import com.example.myapplication.feature.route.data.repository.RouteRepositoryImpl
import com.example.myapplication.feature.route.domain.repository.RouteRepository
import com.example.myapplication.feature.route.domain.usecase.CreateRouteFromItineraryUseCase
import com.example.myapplication.feature.route.domain.usecase.ExportRouteUseCase
import com.example.myapplication.feature.route.domain.usecase.GetMapMarkersUseCase
import com.example.myapplication.feature.route.domain.usecase.GetRouteUseCase
import com.example.myapplication.feature.route.presentation.RouteViewViewModel
import org.koin.core.module.dsl.viewModel
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
    viewModel {
        RouteViewViewModel(
            getRouteUseCase = get(),
            exportRouteUseCase = get()
        )
    }
}
