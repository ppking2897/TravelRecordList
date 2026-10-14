package com.example.myapplication.feature.draft.di

import com.example.myapplication.feature.draft.data.repository.DraftRepositoryImpl
import com.example.myapplication.feature.draft.domain.repository.DraftRepository
import com.example.myapplication.feature.draft.domain.usecase.DeleteDraftUseCase
import com.example.myapplication.feature.draft.domain.usecase.LoadDraftUseCase
import com.example.myapplication.feature.draft.domain.usecase.SaveDraftUseCase
import kotlin.time.ExperimentalTime
import org.koin.dsl.module

@ExperimentalTime
val draftModule = module {
    // Repositories
    single<DraftRepository> { DraftRepositoryImpl(get()) }

    // Use Cases
    factory { SaveDraftUseCase(get()) }
    factory { LoadDraftUseCase(get()) }
    factory { DeleteDraftUseCase(get()) }
}
