package io.github.ppking2897.travelrecord.core.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MVI 架構的 ViewModel 基礎類別
 *
 * @param Intent 使用者意圖
 * @param State UI 狀態
 * @param Event 一次性事件（導航、Toast 等）
 * @param initialState 初始狀態
 */
abstract class BaseViewModel<Intent, State, Event>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)

    /**
     * UI 狀態的 StateFlow，UI 收集此 Flow 觀察狀態變化
     */
    val state: StateFlow<State> = _state.asStateFlow()

    private val _event = Channel<Event>()

    /**
     * UI 事件的 Flow，UI 收集此 Flow 處理一次性事件
     */
    val event: Flow<Event> = _event.receiveAsFlow()

    /**
     * 處理 Intent，Screen 透過此方法送出使用者意圖
     */
    abstract fun processIntent(intent: Intent)

    /**
     * 以 reducer 產生新的 State，確保 State 不可變
     */
    protected fun updateState(reducer: State.() -> State) {
        _state.update { it.reducer() }
    }

    /**
     * 發送一次性 Event，每個事件只會被收到一次
     */
    protected fun sendEvent(event: Event) {
        viewModelScope.launch {
            _event.send(event)
        }
    }
}
