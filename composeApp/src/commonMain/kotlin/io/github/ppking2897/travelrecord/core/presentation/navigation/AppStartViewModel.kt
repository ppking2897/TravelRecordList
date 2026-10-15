package io.github.ppking2897.travelrecord.core.presentation.navigation

import androidx.lifecycle.viewModelScope
import io.github.ppking2897.travelrecord.core.presentation.mvi.BaseViewModel
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetCurrentTripUseCase
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * 決定 App 啟動時停在哪個分頁：有進行中的旅行就停在「今日」，否則停在「行程」
 *
 * 只在 ViewModel 建立時判斷一次；旋轉螢幕時 ViewModel 存活，不會重新判斷。
 */
class AppStartViewModel(
    private val getCurrentTripUseCase: GetCurrentTripUseCase,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : BaseViewModel<AppStartIntent, AppStartState, AppStartEvent>(
    initialState = AppStartState()
) {

    init {
        processIntent(AppStartIntent.DecideStartTab)
    }

    override fun processIntent(intent: AppStartIntent) {
        when (intent) {
            AppStartIntent.DecideStartTab -> decideStartTab()
        }
    }

    private fun decideStartTab() {
        viewModelScope.launch {
            val currentTrip = getCurrentTripUseCase(clock.todayIn(timeZone)).getOrNull()
            updateState {
                copy(startTab = if (currentTrip != null) AppTab.Today else AppTab.Plan)
            }
        }
    }
}
