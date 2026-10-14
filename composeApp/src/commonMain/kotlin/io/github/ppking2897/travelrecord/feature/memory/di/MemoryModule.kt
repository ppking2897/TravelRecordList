@file:OptIn(kotlin.time.ExperimentalTime::class)
package io.github.ppking2897.travelrecord.feature.memory.di

import io.github.ppking2897.travelrecord.feature.memory.data.service.LocalRuleBasedMemoryStoryGenerator
import io.github.ppking2897.travelrecord.feature.memory.domain.service.MemoryStoryGenerator
import io.github.ppking2897.travelrecord.feature.memory.domain.usecase.GetMemoriesUseCase
import io.github.ppking2897.travelrecord.feature.memory.presentation.MemoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val memoryModule = module {
    single<MemoryStoryGenerator> { LocalRuleBasedMemoryStoryGenerator() }
    factory { GetMemoriesUseCase(get(), get(), get()) }

    viewModel {
        MemoryViewModel(
            getMemoriesUseCase = get()
        )
    }
}
