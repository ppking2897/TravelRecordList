package com.example.myapplication.core.itinerary.di

import com.example.myapplication.core.itinerary.data.repository.ItineraryRepositoryImpl
import com.example.myapplication.core.itinerary.domain.repository.ItineraryRepository
import com.example.myapplication.core.itinerary.domain.usecase.CreateItineraryUseCase
import com.example.myapplication.core.itinerary.domain.usecase.DeleteItineraryUseCase
import com.example.myapplication.core.itinerary.domain.usecase.GetAllItinerariesUseCase
import com.example.myapplication.core.itinerary.domain.usecase.GetItineraryUseCase
import com.example.myapplication.core.itinerary.domain.usecase.InitializeSampleDataUseCase
import com.example.myapplication.core.itinerary.domain.usecase.SearchItinerariesUseCase
import com.example.myapplication.core.itinerary.domain.usecase.UpdateItineraryUseCase
import kotlin.time.ExperimentalTime
import org.koin.dsl.module

@ExperimentalTime
val itineraryModule = module {
    // Repositories
    single<ItineraryRepository> { ItineraryRepositoryImpl(get()) }

    // Use Cases
    factory { GetItineraryUseCase(get()) }
    factory { GetAllItinerariesUseCase(get()) }
    factory { CreateItineraryUseCase(get()) }
    factory { UpdateItineraryUseCase(get()) }
    factory { DeleteItineraryUseCase(get(), get()) }
    factory { SearchItinerariesUseCase(get()) }
    factory { InitializeSampleDataUseCase(get(), get()) }
}
