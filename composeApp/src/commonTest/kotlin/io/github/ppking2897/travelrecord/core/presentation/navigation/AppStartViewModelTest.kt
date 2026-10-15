@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.core.presentation.navigation

import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.usecase.GetCurrentTripUseCase
import io.github.ppking2897.travelrecord.test.failingItineraryRepository
import io.github.ppking2897.travelrecord.test.stubItineraryRepository
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class AppStartViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** 固定在 2026-10-15 上午（UTC），所以「今天」是 10/15 */
    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-10-15T03:00:00Z")
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `判斷完成前還沒有起始分頁`() = runTest(dispatcher) {
        val viewModel = viewModelWith(stubItineraryRepository(inProgressTrip()))

        viewModel.state.value.startTab shouldBe null
    }

    @Test
    fun `有旅行中的行程時，起始分頁是今日`() = runTest(dispatcher) {
        val viewModel = viewModelWith(stubItineraryRepository(inProgressTrip()))

        advanceUntilIdle()

        viewModel.state.value.startTab shouldBe AppTab.Today
    }

    @Test
    fun `沒有旅行中的行程時，起始分頁是行程`() = runTest(dispatcher) {
        val viewModel = viewModelWith(
            stubItineraryRepository(trip("future", LocalDate(2026, 11, 1), LocalDate(2026, 11, 5)))
        )

        advanceUntilIdle()

        viewModel.state.value.startTab shouldBe AppTab.Plan
    }

    @Test
    fun `還沒有任何行程時（第一次安裝），起始分頁是行程`() = runTest(dispatcher) {
        val viewModel = viewModelWith(stubItineraryRepository())

        advanceUntilIdle()

        viewModel.state.value.startTab shouldBe AppTab.Plan
    }

    @Test
    fun `讀取行程失敗時，起始分頁是行程`() = runTest(dispatcher) {
        val viewModel = viewModelWith(failingItineraryRepository())

        advanceUntilIdle()

        viewModel.state.value.startTab shouldBe AppTab.Plan
    }

    private fun viewModelWith(repository: ItineraryRepository) =
        AppStartViewModel(GetCurrentTripUseCase(repository), fixedClock, TimeZone.UTC)

    private fun inProgressTrip() = trip("kyoto", LocalDate(2026, 10, 14), LocalDate(2026, 10, 18))

    private fun trip(id: String, start: LocalDate?, end: LocalDate?) =
        testItinerary(id = id).copy(startDate = start, endDate = end)
}
