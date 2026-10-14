package io.github.ppking2897.travelrecord.core.di

import io.github.ppking2897.travelrecord.core.data.remote.GooglePlacesService
import io.github.ppking2897.travelrecord.core.data.sync.SyncManager
import io.github.ppking2897.travelrecord.core.domain.service.LocationSearchService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val coreModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
    single<LocationSearchService> { GooglePlacesService(get()) }
    single { SyncManager(get()) }
}
