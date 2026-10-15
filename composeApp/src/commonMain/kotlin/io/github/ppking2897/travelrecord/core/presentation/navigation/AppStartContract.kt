package io.github.ppking2897.travelrecord.core.presentation.navigation

/**
 * App 啟動時要停在哪個分頁
 *
 * @property startTab 判斷結果；null 表示還在判斷
 */
data class AppStartState(
    val startTab: AppTab? = null
)

sealed class AppStartIntent {
    data object DecideStartTab : AppStartIntent()
}

/** 目前沒有一次性事件 */
sealed class AppStartEvent
