package com.example.myapplication.core.di

import com.example.myapplication.core.data.remote.GooglePlacesService
import com.example.myapplication.core.data.sync.SyncManager
import com.example.myapplication.core.domain.service.LocationSearchService
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
