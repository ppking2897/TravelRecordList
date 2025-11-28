package com.example.myapplication.ui.mvi

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.coroutines.withTimeout

/**
 * BaseViewModel 測試基礎類別
 * 
 * 提供測試 ViewModel 的輔助方法
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModelTest(body: StringSpec.() -> Unit) : StringSpec(body) {
    
    protected val testDispatcher = StandardTestDispatcher()
    protected val testScope = TestScope(testDispatcher)
    
    init {
        beforeTest {
            Dispatchers.setMain(testDispatcher)
        }
        
        afterTest {
            Dispatchers.resetMain()
        }
    }
    
    /**
     * 等待所有協程完成
     */
    protected suspend fun advanceUntilIdle() {
        testDispatcher.scheduler.advanceUntilIdle()
    }
    
    /**
     * 推進虛擬時間
     */
    protected suspend fun advanceTimeBy(delayTimeMillis: Long) {
        testDispatcher.scheduler.advanceTimeBy(delayTimeMillis)
    }
}

/**
 * 測試用的 State
 */
data class TestState(
    val value: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

/**
 * 測試用的 Intent
 */
sealed class TestIntent : UiIntent {
    data class UpdateValue(val value: String) : TestIntent()
    object Load : TestIntent()
    object Fail : TestIntent()
}

/**
 * 測試用的 Event
 */
sealed class TestEvent : UiEvent {
    data class ShowMessage(val message: String) : TestEvent()
    object Navigate : TestEvent()
}

/**
 * 測試用的 ViewModel
 */
class TestViewModel : BaseViewModel<TestState, TestIntent, TestEvent>(
    initialState = TestState()
) {
    override suspend fun processIntent(intent: TestIntent) {
        when (intent) {
            is TestIntent.UpdateValue -> {
                updateState { copy(value = intent.value) }
            }
            is TestIntent.Load -> {
                updateState { copy(isLoading = true) }
                kotlinx.coroutines.delay(100)
                updateState { copy(isLoading = false, value = "loaded") }
            }
            is TestIntent.Fail -> {
                updateState { copy(error = "Failed") }
                sendEvent(TestEvent.ShowMessage("Error occurred"))
            }
        }
    }
}

/**
 * BaseViewModel 基本功能測試
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelBasicTest : BaseViewModelTest({
    
    "should have initial state" {
        val viewModel = TestViewModel()
        viewModel.state.value shouldBe TestState()
    }
    
    "should update state when intent is handled" {
        runTest {
            val viewModel = TestViewModel()
            
            viewModel.handleIntent(TestIntent.UpdateValue("test"))
            advanceUntilIdle()
            
            viewModel.state.value.value shouldBe "test"
        }
    }
    
    "should emit event when sendEvent is called" {
        runTest {
            val viewModel = TestViewModel()
            
            viewModel.handleIntent(TestIntent.Fail)
            advanceUntilIdle()
            
            val receivedEvent = withTimeout(1000) {
                viewModel.event.first()
            }
            
            receivedEvent shouldBe TestEvent.ShowMessage("Error occurred")
        }
    }
    
    "should maintain state immutability" {
        runTest {
            val viewModel = TestViewModel()
            val stateBefore = viewModel.state.value
            
            viewModel.handleIntent(TestIntent.UpdateValue("new"))
            advanceUntilIdle()
            
            // Original state should not change
            stateBefore shouldBe TestState()
            // New state should be different
            viewModel.state.value shouldNotBe stateBefore
        }
    }
    
    "should handle loading state correctly" {
        runTest {
            val viewModel = TestViewModel()
            
            viewModel.handleIntent(TestIntent.Load)
            advanceTimeBy(50)
            
            // During loading
            viewModel.state.value.isLoading shouldBe true
            
            advanceUntilIdle()
            
            // After loading
            viewModel.state.value.isLoading shouldBe false
            viewModel.state.value.value shouldBe "loaded"
        }
    }
})
