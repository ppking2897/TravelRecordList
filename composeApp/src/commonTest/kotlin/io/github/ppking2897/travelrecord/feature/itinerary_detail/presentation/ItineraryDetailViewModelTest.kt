@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.feature.itinerary_detail.presentation

import io.github.ppking2897.travelrecord.core.domain.service.ImageStorageService
import io.github.ppking2897.travelrecord.feature.itinerary.domain.model.Itinerary
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.Hashtag
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.model.ItineraryItem
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.repository.ItineraryItemRepository
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.FilterItemsByDateUseCase
import io.github.ppking2897.travelrecord.feature.itinerary_item.domain.usecase.GroupItemsByDateUseCase
import io.github.ppking2897.travelrecord.test.FailingDeleteItemRepository
import io.github.ppking2897.travelrecord.test.FailingDeleteItineraryRepository
import io.github.ppking2897.travelrecord.test.FailingReadItemRepository
import io.github.ppking2897.travelrecord.test.FailingSaveImageStorageService
import io.github.ppking2897.travelrecord.test.FailingUpdateItemRepository
import io.github.ppking2897.travelrecord.test.FakeImageStorageService
import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.SlowReadItemRepository
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.instantAt
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.github.ppking2897.travelrecord.test.testPhoto
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 行程詳情的「邏輯」Scenario，測試名稱對應 .claude/docs/ItineraryDetail.md 的 Scenario
 */
class ItineraryDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** 現在固定是 2026-10-15 10:00 */
    private val now = Instant.parse("2026-10-15T10:00:00Z")
    private val clock = object : Clock {
        override fun now(): Instant = this@ItineraryDetailViewModelTest.now
    }

    private val oct14 = LocalDate(2026, 10, 14)
    private val oct15 = LocalDate(2026, 10, 15)
    private val oct16 = LocalDate(2026, 10, 16)
    private val kyoto = testItinerary("kyoto", title = "京都大阪 5 日遊").copy(startDate = oct14, endDate = LocalDate(2026, 10, 18))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── 讀取行程與項目 ──

    @Test
    fun `讀取成功`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15)))

        viewModel.state.value.itinerary?.title shouldBe "京都大阪 5 日遊"
        viewModel.state.value.groups shouldBe listOf(oct14 to listOf("伏見稻荷"), oct15 to listOf("金閣寺"))
        viewModel.state.value.isLoading shouldBe false
        viewModel.state.value.dateRange shouldBe oct14..LocalDate(2026, 10, 18)
    }

    @Test
    fun `日期沒填完整時不顯示時間軸`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("kinkaku", "金閣寺", oct15), trip = kyoto.copy(endDate = null)))

        viewModel.state.value.dateRange shouldBe null
    }

    @Test
    fun `找不到行程`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(viewModel, ItineraryDetailIntent.LoadItinerary("missing"))

        viewModel.state.value.error shouldBe "找不到行程"
    }

    @Test
    fun `讀取項目失敗`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("kinkaku", "金閣寺", oct15), wrapItems = { FailingReadItemRepository(it) }))

        viewModel.state.value.itinerary?.title shouldBe "京都大阪 5 日遊"
        viewModel.state.value.error shouldNotBe null
    }

    // ── 項目依日期分組、組內依時間排序 ──

    @Test
    fun `依日期分組`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("kinkaku", "金閣寺", oct15), item("fushimi", "伏見稻荷", oct14)))

        viewModel.state.value.groups.map { it.first } shouldBe listOf(oct14, oct15)
    }

    @Test
    fun `組內依時間排序，沒排時間的排最後`() = runTest(dispatcher) {
        val viewModel = loaded(
            useCasesOf(
                item("kiyomizu", "清水寺", oct14, arrival = LocalTime(14, 30)),
                item("fushimi", "伏見稻荷", oct14, departure = LocalTime(9, 0)),
                item("gion", "祇園晚餐", oct14),
            )
        )

        viewModel.state.value.groups shouldBe listOf(oct14 to listOf("伏見稻荷", "清水寺", "祇園晚餐"))
    }

    // ── 選日期篩選列表 ──

    @Test
    fun `選某一天`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15)))

        act(viewModel, ItineraryDetailIntent.SelectDate(oct15))

        viewModel.state.value.groups shouldBe listOf(oct15 to listOf("金閣寺"))
    }

    @Test
    fun `選總覽`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15)))
        act(viewModel, ItineraryDetailIntent.SelectDate(oct15))

        act(viewModel, ItineraryDetailIntent.SelectDate(null))

        viewModel.state.value.groups shouldBe listOf(oct14 to listOf("伏見稻荷"), oct15 to listOf("金閣寺"))
    }

    @Test
    fun `重新讀取後保留選的日期`() = runTest(dispatcher) {
        val viewModel = loaded(
            useCasesOf(
                item("fushimi", "伏見稻荷", oct14),
                item("kinkaku", "金閣寺", oct15, arrival = LocalTime(9, 0)),
                item("arashiyama", "嵐山", oct15, arrival = LocalTime(14, 0)),
            )
        )
        act(viewModel, ItineraryDetailIntent.SelectDate(oct15))

        act(viewModel, ItineraryDetailIntent.DeleteItem("arashiyama"))

        viewModel.state.value.groups shouldBe listOf(oct15 to listOf("金閣寺"))
    }

    @Test
    fun `時間軸計數不受日期篩選影響`() = runTest(dispatcher) {
        val viewModel = loaded(
            useCasesOf(
                item("fushimi", "伏見稻荷", oct14, arrival = LocalTime(9, 0)),
                item("kiyomizu", "清水寺", oct14, arrival = LocalTime(14, 0)),
                item("kinkaku", "金閣寺", oct15),
            )
        )

        act(viewModel, ItineraryDetailIntent.SelectDate(oct15))

        viewModel.state.value.timelineCountOn(oct14) shouldBe 2
        viewModel.state.value.timelineCountOn(oct15) shouldBe 1
    }

    // ── 標記完成 ──

    @Test
    fun `標記完成`() = runTest(dispatcher) {
        val useCases = useCasesOf(item("kinkaku", "金閣寺", oct15))
        val viewModel = loaded(useCases)

        act(viewModel, ItineraryDetailIntent.ToggleItemCompletion("kinkaku"))

        viewModel.state.value.item("金閣寺").isCompleted shouldBe true
        val saved = useCases.items.getItem("kinkaku").getOrThrow()!!
        saved.isCompleted shouldBe true
        saved.completedAt shouldBe now
    }

    @Test
    fun `標記未完成`() = runTest(dispatcher) {
        val useCases = useCasesOf(
            item("kinkaku", "金閣寺", oct15).copy(isCompleted = true, completedAt = Instant.parse("2026-10-15T08:00:00Z"))
        )
        val viewModel = loaded(useCases)

        act(viewModel, ItineraryDetailIntent.ToggleItemCompletion("kinkaku"))

        viewModel.state.value.item("金閣寺").isCompleted shouldBe false
        val saved = useCases.items.getItem("kinkaku").getOrThrow()!!
        saved.isCompleted shouldBe false
        saved.completedAt shouldBe null
    }

    @Test
    fun `標記完成存檔失敗`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("kinkaku", "金閣寺", oct15), wrapItems = { FailingUpdateItemRepository(it) }))
        val states = collectStates(viewModel)
        val events = collectEvents(viewModel)

        act(viewModel, ItineraryDetailIntent.ToggleItemCompletion("kinkaku"))

        states.map { it.item("金閣寺").isCompleted } shouldContain true
        viewModel.state.value.item("金閣寺").isCompleted shouldBe false
        events.filterIsInstance<ItineraryDetailEvent.ShowError>() shouldHaveSize 1
    }

    @Test
    fun `時間軸完成數跟著更新`() = runTest(dispatcher) {
        val viewModel = loaded(
            useCasesOf(
                item("fushimi", "伏見稻荷", oct14, arrival = LocalTime(9, 0)),
                item("kiyomizu", "清水寺", oct14, arrival = LocalTime(14, 0)),
            )
        )

        act(viewModel, ItineraryDetailIntent.ToggleItemCompletion("fushimi"))

        viewModel.state.value.timelineCompletedOn(oct14) shouldBe 1
    }

    // ── 展開項目卡片 ──

    @Test
    fun `展開與收合`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15)))

        act(viewModel, ItineraryDetailIntent.ToggleItemExpansion("kinkaku"), ItineraryDetailIntent.ToggleItemExpansion("fushimi"))

        viewModel.state.value.expandedItemIds shouldBe setOf("kinkaku", "fushimi")

        act(viewModel, ItineraryDetailIntent.ToggleItemExpansion("kinkaku"))

        viewModel.state.value.expandedItemIds shouldBe setOf("fushimi")
    }

    // ── 刪除項目 ──

    @Test
    fun `確認後刪除項目`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15)))

        act(viewModel, ItineraryDetailIntent.DeleteItem("kinkaku"))

        viewModel.state.value.names shouldBe listOf("伏見稻荷")
    }

    @Test
    fun `刪除項目失敗`() = runTest(dispatcher) {
        val viewModel = loaded(
            useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15), wrapItems = { FailingDeleteItemRepository(it) })
        )
        val events = collectEvents(viewModel)

        act(viewModel, ItineraryDetailIntent.DeleteItem("kinkaku"))

        events.filterIsInstance<ItineraryDetailEvent.ShowError>() shouldHaveSize 1
        viewModel.state.value.names shouldContain "金閣寺"
    }

    // ── 刪除整趟行程 ──

    @Test
    fun `確認後刪除行程`() = runTest(dispatcher) {
        val useCases = useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15))
        val viewModel = loaded(useCases)
        val events = collectEvents(viewModel)

        act(viewModel, ItineraryDetailIntent.DeleteItinerary("kyoto"))

        events shouldContain ItineraryDetailEvent.NavigateBack
        useCases.itineraries.getItinerary("kyoto").getOrThrow() shouldBe null
        useCases.items.getItemsByItinerary("kyoto").getOrThrow().shouldBeEmpty()
    }

    @Test
    fun `刪除行程失敗`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("kinkaku", "金閣寺", oct15), wrapItineraries = { FailingDeleteItineraryRepository(it) }))
        val events = collectEvents(viewModel)

        act(viewModel, ItineraryDetailIntent.DeleteItinerary("kyoto"))

        events.filterIsInstance<ItineraryDetailEvent.ShowError>() shouldHaveSize 1
        events shouldNotContain ItineraryDetailEvent.NavigateBack
    }

    // ── 生成路線 ──

    @Test
    fun `生成路線成功`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15)))
        val events = collectEvents(viewModel)

        act(viewModel, ItineraryDetailIntent.GenerateRoute)

        events.filterIsInstance<ItineraryDetailEvent.NavigateToRoute>() shouldBe
            listOf(ItineraryDetailEvent.NavigateToRoute("kyoto"))
    }

    @Test
    fun `地點不足`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("kinkaku", "金閣寺", oct15)))
        val events = collectEvents(viewModel)

        act(viewModel, ItineraryDetailIntent.GenerateRoute)

        events.filterIsInstance<ItineraryDetailEvent.ShowError>() shouldBe
            listOf(ItineraryDetailEvent.ShowError("至少要有兩個不同的地點才能看路線"))
        events.filterIsInstance<ItineraryDetailEvent.NavigateToRoute>().shouldBeEmpty()
    }

    // ── 新增項目時帶入日期 ──

    @Test
    fun `新增這天`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf())
        val events = collectEvents(viewModel)

        act(viewModel, ItineraryDetailIntent.QuickAddItem(afterDayIndex = 2))

        events shouldBe listOf(ItineraryDetailEvent.NavigateToQuickAddItem(oct16))
    }

    // ── 管理項目照片 ──

    @Test
    fun `新增照片`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("kinkaku", "金閣寺", oct15)))

        act(viewModel, ItineraryDetailIntent.AddPhoto("kinkaku", byteArrayOf(1, 2, 3)))

        val kinkaku = viewModel.state.value.item("金閣寺")
        kinkaku.photos shouldHaveSize 1
        kinkaku.coverPhotoId shouldBe kinkaku.photos.single().id
    }

    @Test
    fun `新增照片失敗`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("kinkaku", "金閣寺", oct15), imageStorage = FailingSaveImageStorageService()))
        val events = collectEvents(viewModel)

        act(viewModel, ItineraryDetailIntent.AddPhoto("kinkaku", byteArrayOf(1, 2, 3)))

        events.filterIsInstance<ItineraryDetailEvent.ShowError>() shouldHaveSize 1
        viewModel.state.value.item("金閣寺").photos.shouldBeEmpty()
    }

    @Test
    fun `設為封面`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(kinkakuWithPhotos()))

        act(viewModel, ItineraryDetailIntent.SetCoverPhoto("kinkaku", "photo-b"))

        viewModel.state.value.item("金閣寺").coverPhotoId shouldBe "photo-b"
    }

    @Test
    fun `刪除照片`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(kinkakuWithPhotos()))

        act(viewModel, ItineraryDetailIntent.DeletePhoto("photo-a"))

        val kinkaku = viewModel.state.value.item("金閣寺")
        kinkaku.photos.map { it.id } shouldBe listOf("photo-b")
        kinkaku.coverPhotoId shouldBe null
    }

    // ── 選擇模式與批次操作 ──

    @Test
    fun `勾選項目`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15)))
        act(viewModel, ItineraryDetailIntent.ToggleSelectionMode)

        act(
            viewModel,
            ItineraryDetailIntent.ToggleItemSelection("fushimi"),
            ItineraryDetailIntent.ToggleItemSelection("kinkaku"),
            ItineraryDetailIntent.ToggleItemSelection("fushimi"),
        )

        viewModel.state.value.isSelectionMode shouldBe true
        viewModel.state.value.selectedItemIds shouldBe setOf("kinkaku")
    }

    @Test
    fun `批次刪除`() = runTest(dispatcher) {
        val viewModel = loaded(
            useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15), item("arashiyama", "嵐山", oct16))
        )
        val events = collectEvents(viewModel)
        act(
            viewModel,
            ItineraryDetailIntent.ToggleSelectionMode,
            ItineraryDetailIntent.ToggleItemSelection("fushimi"),
            ItineraryDetailIntent.ToggleItemSelection("kinkaku"),
        )

        act(viewModel, ItineraryDetailIntent.BatchDelete)

        viewModel.state.value.names shouldBe listOf("嵐山")
        events shouldContain ItineraryDetailEvent.ShowBatchOperationResult("已刪除 2 個項目")
        viewModel.state.value.isSelectionMode shouldBe false
    }

    @Test
    fun `批次標記完成`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("fushimi", "伏見稻荷", oct14), item("kinkaku", "金閣寺", oct15)))
        val events = collectEvents(viewModel)
        act(
            viewModel,
            ItineraryDetailIntent.ToggleSelectionMode,
            ItineraryDetailIntent.ToggleItemSelection("fushimi"),
            ItineraryDetailIntent.ToggleItemSelection("kinkaku"),
        )

        act(viewModel, ItineraryDetailIntent.BatchMarkComplete)

        viewModel.state.value.item("伏見稻荷").isCompleted shouldBe true
        viewModel.state.value.item("金閣寺").isCompleted shouldBe true
        events shouldContain ItineraryDetailEvent.ShowBatchOperationResult("已標記 2 個項目為完成")
        viewModel.state.value.isSelectionMode shouldBe false
    }

    @Test
    fun `取消選擇模式`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(item("kinkaku", "金閣寺", oct15)))
        act(viewModel, ItineraryDetailIntent.ToggleSelectionMode, ItineraryDetailIntent.ToggleItemSelection("kinkaku"))

        // 畫面的「取消」依序送出清空勾選與切換選擇模式
        act(viewModel, ItineraryDetailIntent.ClearSelection, ItineraryDetailIntent.ToggleSelectionMode)

        viewModel.state.value.selectedItemIds shouldBe emptySet()
        viewModel.state.value.isSelectionMode shouldBe false
    }

    // ── 規格調整第 4 組：沒日期的行程提示補日期（.claude/docs/SpecAdjustments.md）──

    @Test
    fun `沒日期的行程在詳情頁提示補日期`() = runTest(dispatcher) {
        val viewModel = loadedHokkaido(useCasesOf(trip = hokkaido))

        viewModel.state.value.needsDates shouldBe true
    }

    @Test
    fun `只填了一個日期的行程也提示`() = runTest(dispatcher) {
        val viewModel = loadedHokkaido(useCasesOf(trip = hokkaido.copy(startDate = dec1)))

        viewModel.state.value.needsDates shouldBe true
    }

    @Test
    fun `補完日期回到詳情後提示消失`() = runTest(dispatcher) {
        val useCases = useCasesOf(trip = hokkaido)
        val viewModel = loadedHokkaido(useCases)
        viewModel.state.value.needsDates shouldBe true

        useCases.updateItinerary(hokkaido.copy(startDate = dec1, endDate = dec5), now).getOrThrow()
        act(viewModel, ItineraryDetailIntent.LoadItinerary("hokkaido"))

        viewModel.state.value.needsDates shouldBe false
        viewModel.state.value.dateRange shouldBe dec1..dec5
    }

    @Test
    fun `有日期的行程不顯示提示`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf())

        viewModel.state.value.needsDates shouldBe false
    }

    // ── 測試工具 ──

    private val dec1 = LocalDate(2026, 12, 1)
    private val dec5 = LocalDate(2026, 12, 5)

    // ── 點標籤篩選同標籤的項目（SpecAdjustments.md 第 6 組） ──

    @Test
    fun `點標籤只列出同標籤的項目`() = runTest(dispatcher) {
        val viewModel = loaded(ramenTrip())

        act(viewModel, ItineraryDetailIntent.ToggleHashtagFilter("拉麵"))

        viewModel.state.value.groups shouldBe listOf(oct14 to listOf("一蘭拉麵"), oct16 to listOf("天下一品"))
        viewModel.state.value.selectedHashtag shouldBe "拉麵"
    }

    @Test
    fun `再點一次取消篩選`() = runTest(dispatcher) {
        val viewModel = loaded(ramenTrip())

        act(viewModel, ItineraryDetailIntent.ToggleHashtagFilter("拉麵"), ItineraryDetailIntent.ToggleHashtagFilter("拉麵"))

        viewModel.state.value.groups shouldBe allRamenTripGroups
        viewModel.state.value.selectedHashtag shouldBe null

        // 按篩選條的 ✕ 也一樣
        act(viewModel, ItineraryDetailIntent.ToggleHashtagFilter("拉麵"), ItineraryDetailIntent.ClearHashtagFilter)

        viewModel.state.value.groups shouldBe allRamenTripGroups
        viewModel.state.value.selectedHashtag shouldBe null
    }

    @Test
    fun `點別的標籤換成新的篩選`() = runTest(dispatcher) {
        val viewModel = loaded(ramenTrip())

        act(viewModel, ItineraryDetailIntent.ToggleHashtagFilter("拉麵"), ItineraryDetailIntent.ToggleHashtagFilter("宵夜"))

        viewModel.state.value.groups shouldBe listOf(oct14 to listOf("一蘭拉麵"))
        viewModel.state.value.selectedHashtag shouldBe "宵夜"
    }

    @Test
    fun `點標籤時看整趟行程`() = runTest(dispatcher) {
        val viewModel = loaded(ramenTrip())

        act(viewModel, ItineraryDetailIntent.SelectDate(oct14), ItineraryDetailIntent.ToggleHashtagFilter("拉麵"))

        viewModel.state.value.selectedDate shouldBe null
        viewModel.state.value.groups shouldBe listOf(oct14 to listOf("一蘭拉麵"), oct16 to listOf("天下一品"))
    }

    @Test
    fun `選日期還在讀取時點標籤`() = runTest(dispatcher) {
        val viewModel = loaded(useCasesOf(ramenIchiran, ramenFushimi, ramenTenkaippin, wrapItems = { SlowReadItemRepository(it) }))

        // 點第 3 天，資料還在讀就點 #拉麵
        viewModel.processIntent(ItineraryDetailIntent.SelectDate(oct16))
        runCurrent()
        viewModel.processIntent(ItineraryDetailIntent.ToggleHashtagFilter("拉麵"))
        advanceUntilIdle()

        viewModel.state.value.selectedDate shouldBe null
        viewModel.state.value.groups shouldBe listOf(oct14 to listOf("一蘭拉麵"), oct16 to listOf("天下一品"))
    }

    @Test
    fun `篩選中選某一天，兩個條件一起套用`() = runTest(dispatcher) {
        val viewModel = loaded(ramenTrip())

        act(viewModel, ItineraryDetailIntent.ToggleHashtagFilter("拉麵"), ItineraryDetailIntent.SelectDate(oct16))

        viewModel.state.value.groups shouldBe listOf(oct16 to listOf("天下一品"))
        viewModel.state.value.selectedHashtag shouldBe "拉麵"

        // 再點「全部」
        act(viewModel, ItineraryDetailIntent.SelectDate(null))

        viewModel.state.value.groups shouldBe listOf(oct14 to listOf("一蘭拉麵"), oct16 to listOf("天下一品"))
    }

    @Test
    fun `刪除項目後篩選還在`() = runTest(dispatcher) {
        val viewModel = loaded(ramenTrip())

        act(viewModel, ItineraryDetailIntent.ToggleHashtagFilter("拉麵"), ItineraryDetailIntent.DeleteItem("tenkaippin"))

        viewModel.state.value.selectedHashtag shouldBe "拉麵"
        viewModel.state.value.groups shouldBe listOf(oct14 to listOf("一蘭拉麵"))
        // 刪除後重新讀取，時間軸照樣算整天：10/14 還有一蘭拉麵與伏見稻荷
        viewModel.state.value.timelineCountOn(oct14) shouldBe 2
    }

    @Test
    fun `編輯項目回來後篩選還在`() = runTest(dispatcher) {
        val useCases = ramenTrip()
        val viewModel = loaded(useCases)
        act(viewModel, ItineraryDetailIntent.ToggleHashtagFilter("拉麵"))

        // 在編輯項目頁存檔：備註加上 #拉麵，標籤是存檔時從備註抓出來的
        useCases.updateItem(ramenFushimi.copy(notes = "#神社 #拉麵"), currentTimestamp = now).getOrThrow()
        // 回到行程詳情時畫面會再讀一次
        act(viewModel, ItineraryDetailIntent.LoadItinerary("kyoto"))

        viewModel.state.value.selectedHashtag shouldBe "拉麵"
        viewModel.state.value.groups shouldBe listOf(oct14 to listOf("一蘭拉麵", "伏見稻荷"), oct16 to listOf("天下一品"))
    }

    @Test
    fun `篩不到項目時說明原因`() = runTest(dispatcher) {
        // 只有這條情境：10/15 另有兩個沒有標籤的項目
        val viewModel = loaded(
            useCasesOf(
                ramenIchiran, ramenFushimi, ramenTenkaippin,
                item("kinkaku", "金閣寺", oct15), item("chikurin", "竹林", oct15),
            )
        )

        act(viewModel, ItineraryDetailIntent.ToggleHashtagFilter("拉麵"), ItineraryDetailIntent.SelectDate(oct15))

        // 畫面依「列表是空的、還在篩標籤」顯示「沒有 #拉麵 的項目」
        viewModel.state.value.groupedItems.shouldBeEmpty()
        viewModel.state.value.selectedHashtag shouldBe "拉麵"
        // 當日摘要卡與時間軸照樣算整天
        viewModel.state.value.timelineCountOn(oct15) shouldBe 2
    }

    /** 舊版留下、沒有日期的行程 */
    private val hokkaido = testItinerary("hokkaido", title = "北海道").copy(startDate = null, endDate = null)

    /** 建立 ViewModel 並讀取北海道這趟行程 */
    private fun TestScope.loadedHokkaido(useCases: TestUseCases): ItineraryDetailViewModel {
        val viewModel = viewModelWith(useCases)
        act(viewModel, ItineraryDetailIntent.LoadItinerary("hokkaido"))
        return viewModel
    }

    private fun item(
        id: String,
        location: String,
        date: LocalDate,
        arrival: LocalTime? = null,
        departure: LocalTime? = null,
    ): ItineraryItem = testItem(id, "kyoto", date = date, arrivalTime = arrival, departureTime = departure, locationName = location)

    /** 項目的備註寫著這些 #標籤，存檔時抓出的標籤也一起放好 */
    private fun tagged(id: String, location: String, date: LocalDate, vararg tags: String): ItineraryItem =
        item(id, location, date).copy(
            notes = tags.joinToString(" ") { "#$it" },
            hashtags = tags.map { Hashtag(tag = it, usageCount = 1, firstUsed = instantAt(0), lastUsed = instantAt(0)) },
        )

    // 第 6 組情境的資料
    private val ramenIchiran = tagged("ichiran", "一蘭拉麵", oct14, "拉麵", "宵夜")
    private val ramenFushimi = tagged("fushimi", "伏見稻荷", oct14, "神社")
    private val ramenTenkaippin = tagged("tenkaippin", "天下一品", oct16, "拉麵")
    private val allRamenTripGroups = listOf(oct14 to listOf("一蘭拉麵", "伏見稻荷"), oct16 to listOf("天下一品"))

    private fun ramenTrip() = useCasesOf(ramenIchiran, ramenFushimi, ramenTenkaippin)

    /** 金閣寺有照片 A（封面）與 B */
    private fun kinkakuWithPhotos(): ItineraryItem =
        item("kinkaku", "金閣寺", oct15).copy(
            photos = listOf(testPhoto("photo-a", "kinkaku", order = 0), testPhoto("photo-b", "kinkaku", order = 1)),
            coverPhotoId = "photo-a",
        )

    /** 行程與項目共用同一個項目 Repository，跟 App 裡的資料庫一樣 */
    private fun useCasesOf(
        vararg items: ItineraryItem,
        trip: Itinerary = kyoto,
        imageStorage: ImageStorageService = FakeImageStorageService(),
        wrapItems: (ItineraryItemRepository) -> ItineraryItemRepository = { it },
        wrapItineraries: (ItineraryRepository) -> ItineraryRepository = { it },
    ): TestUseCases {
        val stored = InMemoryItineraryItemRepository(*items)
        val itineraries = wrapItineraries(InMemoryItineraryRepository(stored, trip))
        return TestUseCases(itineraries, wrapItems(stored), imageStorage)
    }

    private fun viewModelWith(useCases: TestUseCases) = ItineraryDetailViewModel(
        getItineraryUseCase = useCases.getItinerary,
        getItemsByItineraryUseCase = useCases.getItemsByItinerary,
        deleteItineraryUseCase = useCases.deleteItinerary,
        getRouteForItineraryUseCase = useCases.getRouteForItinerary,
        updateItemUseCase = useCases.updateItem,
        deleteItemUseCase = useCases.deleteItem,
        groupItemsByDateUseCase = GroupItemsByDateUseCase(),
        filterItemsByDateUseCase = FilterItemsByDateUseCase(),
        addPhotoUseCase = useCases.addPhoto,
        deletePhotoUseCase = useCases.deletePhoto,
        setCoverPhotoUseCase = useCases.setCoverPhoto,
        generateThumbnailUseCase = useCases.generateThumbnail,
        batchDeleteItemsUseCase = useCases.batchDeleteItems,
        batchUpdateItemsUseCase = useCases.batchUpdateItems,
        clock = clock,
    )

    /** 建立 ViewModel 並讀取京都這趟行程 */
    private fun TestScope.loaded(useCases: TestUseCases): ItineraryDetailViewModel {
        val viewModel = viewModelWith(useCases)
        act(viewModel, ItineraryDetailIntent.LoadItinerary("kyoto"))
        return viewModel
    }

    /** 依序送出指令，每送一個就把排程跑完 */
    private fun TestScope.act(viewModel: ItineraryDetailViewModel, vararg intents: ItineraryDetailIntent) {
        intents.forEach {
            viewModel.processIntent(it)
            advanceUntilIdle()
        }
    }

    private fun TestScope.collectStates(viewModel: ItineraryDetailViewModel): List<ItineraryDetailState> {
        val states = mutableListOf<ItineraryDetailState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { states += it }
        }
        return states
    }

    private fun TestScope.collectEvents(viewModel: ItineraryDetailViewModel): List<ItineraryDetailEvent> {
        val events = mutableListOf<ItineraryDetailEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.collect { events += it }
        }
        return events
    }

    /** 列表顯示的分組：日期 → 地點名稱 */
    private val ItineraryDetailState.groups: List<Pair<LocalDate, List<String>>>
        get() = groupedItems.map { group -> group.date to group.items.map { it.location.name } }

    private val ItineraryDetailState.names: List<String>
        get() = groupedItems.flatMap { group -> group.items.map { it.location.name } }

    private fun ItineraryDetailState.item(locationName: String): ItineraryItem =
        groupedItems.flatMap { it.items }.single { it.location.name == locationName }

    /** 時間軸上某天的項目數 */
    private fun ItineraryDetailState.timelineCountOn(date: LocalDate): Int =
        allGroupedItems.single { it.date == date }.items.size

    /** 時間軸上某天的已完成數 */
    private fun ItineraryDetailState.timelineCompletedOn(date: LocalDate): Int =
        allGroupedItems.single { it.date == date }.items.count { it.isCompleted }
}
