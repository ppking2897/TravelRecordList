@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.feature.itinerary_item.presentation

import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.github.ppking2897.travelrecord.test.testPhoto
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * 編輯項目的「邏輯」情境，測試名稱對應 .claude/docs/SpecAdjustments.md 第 3 組的 Scenario
 */
class EditItemViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val kyoto = testItinerary("kyoto", title = "京都大阪 5 日遊")

    /** 地址、座標、備註都沒填，有時間、有一張照片 */
    private val kiyomizu = testItem(
        "kiyomizu", "kyoto",
        arrivalTime = LocalTime(10, 0),
        departureTime = LocalTime(11, 0),
        locationName = "清水寺",
        activity = "參拜",
    ).copy(photos = listOf(testPhoto("p1", "kiyomizu")))

    private val useCases = run {
        val itemRepository = InMemoryItineraryItemRepository(kiyomizu)
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

    // ── 編輯沒存就離開會提醒 ──

    @Test
    fun `編輯項目沒改就離開`() = runTest(dispatcher) {
        val viewModel = editViewModel()

        act(viewModel, EditItemIntent.LoadItem("kiyomizu"))

        viewModel.state.value.hasUnsavedChanges shouldBe false
    }

    @Test
    fun `項目的每一欄都算`() = runTest(dispatcher) {
        val edits = listOf(
            "活動" to EditItemIntent.UpdateActivity("參拜本堂"),
            "地點" to EditItemIntent.UpdateLocationName("清水寺 本堂"),
            "地址" to EditItemIntent.UpdateLocationAddress("京都市東山區清水1丁目294"),
            "備註" to EditItemIntent.UpdateNotes("記得買御守"),
            "日期" to EditItemIntent.UpdateDate(LocalDate(2026, 10, 2)),
            "到達時間" to EditItemIntent.UpdateArrivalTime(LocalTime(9, 30)),
            "離開時間" to EditItemIntent.UpdateDepartureTime(LocalTime(11, 30)),
            "加照片" to EditItemIntent.AddPhoto("fake/p2.jpg"),
            "刪照片" to EditItemIntent.RemovePhoto("fake/p1.jpg"),
        )

        edits.forEach { (field, edit) ->
            val viewModel = editViewModel()
            act(viewModel, EditItemIntent.LoadItem("kiyomizu"), edit)
            withClue(field) { viewModel.state.value.hasUnsavedChanges shouldBe true }
        }
    }

    // ── 測試工具 ──

    private fun editViewModel() = EditItemViewModel(
        useCases.updateItem,
        useCases.getItem,
        useCases.getItinerary,
        useCases.imageStorage,
    )

    /** 依序送出指令，每送一個就把排程跑完 */
    private fun TestScope.act(viewModel: EditItemViewModel, vararg intents: EditItemIntent) {
        intents.forEach {
            viewModel.processIntent(it)
            advanceUntilIdle()
        }
    }
}
