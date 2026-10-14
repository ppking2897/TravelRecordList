package io.github.ppking2897.travelrecord.feature.draft.di

import io.github.ppking2897.travelrecord.feature.draft.data.repository.DraftRepositoryImpl
import io.github.ppking2897.travelrecord.feature.draft.domain.repository.DraftRepository
import io.github.ppking2897.travelrecord.feature.draft.domain.usecase.DeleteDraftUseCase
import io.github.ppking2897.travelrecord.feature.draft.domain.usecase.LoadDraftUseCase
import io.github.ppking2897.travelrecord.feature.draft.domain.usecase.SaveDraftUseCase
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
