package io.github.ppking2897.travelrecord.feature.itinerary.di

import io.github.ppking2897.travelrecord.feature.itinerary.data.repository.ItineraryRepositoryImpl
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.CreateItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.DeleteItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.EnsureLocalDataReadyUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetAllItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetItineraryUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.InitializeSampleDataUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.SearchItinerariesUseCase
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.UpdateItineraryUseCase
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
    factory { EnsureLocalDataReadyUseCase(get(), get()) }
}
