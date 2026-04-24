@file:OptIn(kotlin.time.ExperimentalTime::class)
package com.example.myapplication.feature.memory.di

import com.example.myapplication.feature.memory.data.service.LocalRuleBasedMemoryStoryGenerator
import com.example.myapplication.feature.memory.domain.service.MemoryStoryGenerator
import com.example.myapplication.feature.memory.domain.usecase.GetMemoriesUseCase
import com.example.myapplication.feature.memory.presentation.MemoryViewModel
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
