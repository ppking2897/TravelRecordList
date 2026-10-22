@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.feature.route.presentation

import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * 路線頁拿行程編號，打開時依行程目前的項目排出路線
 */
class RouteViewViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val stored = InMemoryItineraryItemRepository(
        testItem("fushimi", "kyoto", date = LocalDate(2026, 10, 14), locationName = "伏見稻荷"),
        testItem("kinkaku", "kyoto", date = LocalDate(2026, 10, 15), locationName = "金閣寺"),
    )
    private val useCases = TestUseCases(
        InMemoryItineraryRepository(stored, testItinerary("kyoto", title = "京都 3 日遊")),
        stored,
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = RouteViewViewModel(useCases.getRouteForItinerary, useCases.exportRoute)

    private fun TestScope.act(viewModel: RouteViewViewModel, vararg intents: RouteViewIntent) {
        intents.forEach {
            viewModel.processIntent(it)
            advanceUntilIdle()
        }
    }

    private fun TestScope.collectEvents(viewModel: RouteViewViewModel): List<RouteViewEvent> {
        val events = mutableListOf<RouteViewEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.collect { events += it }
        }
        return events
    }

    @Test
    fun `打開路線頁`() = runTest(dispatcher) {
        val viewModel = viewModel()

        act(viewModel, RouteViewIntent.LoadRoute("kyoto"))

        val state = viewModel.state.value
        state.route?.title shouldBe "京都 3 日遊"
        state.route?.locations?.map { it.location.name } shouldBe listOf("伏見稻荷", "金閣寺")
        state.isLoading shouldBe false
        state.error shouldBe null
    }

    @Test
    fun `找不到行程`() = runTest(dispatcher) {
        val viewModel = viewModel()

        act(viewModel, RouteViewIntent.LoadRoute("missing"))

        viewModel.state.value.route shouldBe null
        viewModel.state.value.error shouldBe "找不到路線"
    }

    @Test
    fun `分享路線`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val events = collectEvents(viewModel)

        act(viewModel, RouteViewIntent.LoadRoute("kyoto"), RouteViewIntent.ExportRoute("kyoto"))

        val exported = events.filterIsInstance<RouteViewEvent.ExportSuccess>()
        exported shouldHaveSize 1
        exported.single().json shouldContain "伏見稻荷"
        exported.single().json shouldContain "金閣寺"
    }
}
