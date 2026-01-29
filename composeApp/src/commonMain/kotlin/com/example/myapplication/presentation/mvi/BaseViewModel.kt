package com.example.myapplication.presentation.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MVI 架構的 ViewModel 基礎類別
 * 
 * @param S State 類型，必須實作 UiState
 * @param I Intent 類型，必須實作 UiIntent
 * @param E Event 類型，必須實作 UiEvent
 * @param initialState 初始狀態
 */
abstract class BaseViewModel<S : UiState, I : UiIntent, E : UiEvent>(
    initialState: S
) : ViewModel() {
    
    // State: 使用 StateFlow 管理 UI 狀態
    private val _state = MutableStateFlow(initialState)
    
    /**
     * UI 狀態的 StateFlow
     * UI 應該收集此 Flow 來觀察狀態變化
     */
    val state: StateFlow<S> = _state.asStateFlow()
    
    // Event: 使用 Channel 處理單次事件
    private val _event = Channel<E>(Channel.BUFFERED)
    
    /**
     * UI 事件的 Flow
     * UI 應該收集此 Flow 來處理單次事件（如導航、顯示 Toast）
     */
    val event: Flow<E> = _event.receiveAsFlow()
    
    /**
     * 處理 Intent
     * 
     * 此方法會在 viewModelScope 中啟動協程來處理 Intent
     * 
     * @param intent 要處理的 Intent
     */
    fun handleIntent(intent: I) {
        viewModelScope.launch {
            processIntent(intent)
        }
    }
    
    /**
     * 處理具體的 Intent
     * 
     * 子類應該實作此方法來處理不同的 Intent
     * 此方法在協程中執行，可以進行非同步操作
     * 
     * @param intent 要處理的 Intent
     */
    protected abstract suspend fun processIntent(intent: I)
    
    /**
     * 更新 State
     * 
     * 使用 reducer 函數來產生新的 State
     * 確保 State 的不可變性
     * 
     * @param reducer State 的轉換函數
     */
    protected fun updateState(reducer: S.() -> S) {
        _state.update { currentState ->
            currentState.reducer()
        }
    }
    
    /**
     * 發送 Event
     * 
     * Event 是單次性的，每個訂閱者只會收到一次
     * 
     * @param event 要發送的 Event
     */
    protected suspend fun sendEvent(event: E) {
        _event.send(event)
    }
    
    /**
     * 取得當前的 State
     * 
     * 用於在處理 Intent 時讀取當前狀態
     */
    protected val currentState: S
        get() = _state.value
}
