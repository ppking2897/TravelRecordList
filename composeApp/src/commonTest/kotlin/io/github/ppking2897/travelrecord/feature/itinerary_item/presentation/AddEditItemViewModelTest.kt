@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package io.github.ppking2897.travelrecord.feature.itinerary_item.presentation

import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.shouldBe
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
import kotlin.time.ExperimentalTime

/**
 * 新增項目的「邏輯」情境，測試名稱對應 .claude/docs/SpecAdjustments.md 第 3 組的 Scenario
 */
class AddEditItemViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val kyoto = testItinerary("kyoto", title = "京都大阪 5 日遊")

    private val useCases = run {
        val itemRepository = InMemoryItineraryItemRepository()
        TestUseCases(InMemoryItineraryRepository(itemRepository, kyoto), itemRepository)
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
    fun `連按儲存只存一次`() = runTest(dispatcher) {
        val viewModel = addViewModel()
        val events = eventsOf(viewModel)
        act(
            viewModel,
            AddEditItemIntent.LoadItinerary("kyoto"),
            AddEditItemIntent.UpdateActivity("參拜"),
            AddEditItemIntent.UpdateLocationName("清水寺"),
            AddEditItemIntent.UpdateDate(LocalDate(2026, 10, 2)),
        )

        // 很快地連按兩下，第二下送出時第一下還沒跑
        viewModel.processIntent(AddEditItemIntent.Save)
        viewModel.processIntent(AddEditItemIntent.Save)
        advanceUntilIdle()

        useCases.getItemsByItinerary("kyoto").getOrThrow().count { it.location.name == "清水寺" } shouldBe 1
        // 每個 SaveSuccess 都會讓畫面回上一頁
        events.count { it == AddEditItemEvent.SaveSuccess } shouldBe 1
    }

    // ── 測試工具 ──

    private fun addViewModel() = AddEditItemViewModel(
        useCases.addItem,
        useCases.getItinerary,
        useCases.imageStorage,
    )

    /** 從現在起收集 [viewModel] 發出的事件 */
    private fun TestScope.eventsOf(viewModel: AddEditItemViewModel): List<AddEditItemEvent> {
        val events = mutableListOf<AddEditItemEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events += it } }
        return events
    }

    /** 依序送出指令，每送一個就把排程跑完 */
    private fun TestScope.act(viewModel: AddEditItemViewModel, vararg intents: AddEditItemIntent) {
        intents.forEach {
            viewModel.processIntent(it)
            advanceUntilIdle()
        }
    }
}
