package com.example.myapplication.ui.mvi

/**
 * 通用的載入狀態包裝
 * 
 * 用於包裝非同步操作的結果
 */
sealed class LoadingState<out T> {
    /**
     * 閒置狀態，尚未開始載入
     */
    object Idle : LoadingState<Nothing>()
    
    /**
     * 載入中
     */
    object Loading : LoadingState<Nothing>()
    
    /**
     * 載入成功
     * @param data 載入的資料
     */
    data class Success<T>(val data: T) : LoadingState<T>()
    
    /**
     * 載入失敗
     * @param message 錯誤訊息
     * @param throwable 可選的例外物件
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : LoadingState<Nothing>()
}

/**
 * 檢查是否為載入中狀態
 */
fun <T> LoadingState<T>.isLoading(): Boolean = this is LoadingState.Loading

/**
 * 檢查是否為成功狀態
 */
fun <T> LoadingState<T>.isSuccess(): Boolean = this is LoadingState.Success

/**
 * 檢查是否為錯誤狀態
 */
fun <T> LoadingState<T>.isError(): Boolean = this is LoadingState.Error

/**
 * 取得成功狀態的資料，如果不是成功狀態則返回 null
 */
fun <T> LoadingState<T>.getOrNull(): T? = when (this) {
    is LoadingState.Success -> data
    else -> null
}
