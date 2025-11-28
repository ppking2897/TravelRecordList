package com.example.myapplication.ui.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * 測試工具函數
 */

/**
 * 收集 StateFlow 的所有值
 */
suspend fun <T> Flow<T>.collectValues(scope: TestScope, count: Int): List<T> {
    val values = mutableListOf<T>()
    val job = scope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
        this@collectValues.toList(values)
    }
    
    // 等待收集指定數量的值
    while (values.size < count) {
        scope.testScheduler.advanceUntilIdle()
    }
    
    job.cancel()
    return values
}

/**
 * 收集單一 Event
 */
suspend fun <T> Flow<T>.collectFirst(scope: TestScope): T? {
    var result: T? = null
    val job = scope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
        this@collectFirst.collect { value ->
            result = value
        }
    }
    
    scope.testScheduler.advanceUntilIdle()
    job.cancel()
    return result
}

/**
 * 收集多個 Event
 */
suspend fun <T> Flow<T>.collectMultiple(scope: TestScope, count: Int): List<T> {
    val events = mutableListOf<T>()
    val job = scope.launch(UnconfinedTestDispatcher(scope.testScheduler)) {
        this@collectMultiple.collect { event ->
            events.add(event)
            if (events.size >= count) {
                throw StopCollectingException()
            }
        }
    }
    
    scope.testScheduler.advanceUntilIdle()
    job.cancel()
    return events
}

private class StopCollectingException : Exception()
