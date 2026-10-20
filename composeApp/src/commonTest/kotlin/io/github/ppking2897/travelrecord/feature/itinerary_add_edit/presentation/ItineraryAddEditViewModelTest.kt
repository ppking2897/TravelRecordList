@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.feature.itinerary_add_edit.presentation

import io.github.ppking2897.travelrecord.feature.draft.domain.model.DraftType
import io.github.ppking2897.travelrecord.feature.draft.domain.repository.DraftRepository
import io.github.ppking2897.travelrecord.feature.itinerary.domain.repository.ItineraryRepository
import io.github.ppking2897.travelrecord.test.CountingDraftRepository
import io.github.ppking2897.travelrecord.test.FailingUpdateItineraryRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.RecordingImageStorageService
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.inMemoryDraftRepository
import io.github.ppking2897.travelrecord.test.testItinerary
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * 建立與編輯行程的「邏輯」情境，測試名稱對應 A 組修正討論時定下的情境
 */
class ItineraryAddEditViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** 原本的封面是 A */
    private val coverA = "covers/A.jpg"
    private val kyoto = testItinerary("kyoto", title = "京都大阪 5 日遊").copy(coverPhotoPath = coverA)

    private val imageStorage = RecordingImageStorageService()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── 封面：原本的封面檔等儲存成功才刪 ──

    @Test
    fun `換封面但沒儲存`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(viewModel, ItineraryAddEditIntent.LoadItinerary("kyoto"), ItineraryAddEditIntent.SetCoverPhoto(photoBytes))

        imageStorage.deleted.shouldBeEmpty()
    }

    @Test
    fun `換封面後儲存`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.SetCoverPhoto(photoBytes),
            ItineraryAddEditIntent.Save,
        )

        val coverB = imageStorage.saved.single()
        imageStorage.deleted shouldBe listOf(coverA)
        useCases.getItinerary("kyoto").getOrThrow()?.coverPhotoPath shouldBe coverB
    }

    @Test
    fun `沒換封面直接儲存`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.UpdateTitle("京都大阪 6 日遊"),
            ItineraryAddEditIntent.Save,
        )

        imageStorage.deleted.shouldBeEmpty()
    }

    @Test
    fun `換封面後儲存失敗`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf(wrap = ::FailingUpdateItineraryRepository))

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.SetCoverPhoto(photoBytes),
            ItineraryAddEditIntent.Save,
        )

        imageStorage.deleted.shouldBeEmpty()
    }

    @Test
    fun `先挑一張再換一張`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.SetCoverPhoto(photoBytes),
            ItineraryAddEditIntent.SetCoverPhoto(photoBytes),
        )

        val coverB = imageStorage.saved.first()
        imageStorage.deleted shouldBe listOf(coverB)
    }

    @Test
    fun `移除封面但沒儲存`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())

        act(viewModel, ItineraryAddEditIntent.LoadItinerary("kyoto"), ItineraryAddEditIntent.RemoveCoverPhoto)

        imageStorage.deleted.shouldBeEmpty()
    }

    @Test
    fun `移除封面後儲存`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val viewModel = viewModelWith(useCases)

        act(
            viewModel,
            ItineraryAddEditIntent.LoadItinerary("kyoto"),
            ItineraryAddEditIntent.RemoveCoverPhoto,
            ItineraryAddEditIntent.Save,
        )

        imageStorage.deleted shouldBe listOf(coverA)
        useCases.getItinerary("kyoto").getOrThrow()?.coverPhotoPath shouldBe null
    }

    // ── 草稿：新增行程時停下打字 1 秒自動存 ──

    @Test
    fun `連續打字只在停下後存一次`() = runTest(dispatcher) {
        val drafts = CountingDraftRepository(inMemoryDraftRepository())
        val useCases = useCasesOf(drafts = drafts)
        val viewModel = viewModelWith(useCases)
        act(viewModel, ItineraryAddEditIntent.LoadDraft)

        type(viewModel, "京")
        advanceTimeBy(500)
        type(viewModel, "京都")
        advanceTimeBy(500)
        type(viewModel, "京都大阪")
        advanceTimeBy(999)
        runCurrent()
        drafts.saveCount shouldBe 0

        advanceTimeBy(2)
        runCurrent()
        drafts.saveCount shouldBe 1
        useCases.loadDraft(DraftType.ITINERARY).getOrThrow()?.get("title") shouldBe "京都大阪"
    }

    @Test
    fun `存好後提示三秒後消失`() = runTest(dispatcher) {
        val viewModel = viewModelWith(useCasesOf())
        act(viewModel, ItineraryAddEditIntent.LoadDraft)

        type(viewModel, "京都大阪")
        advanceTimeBy(1001)
        runCurrent()
        viewModel.state.value.showDraftSaved shouldBe true

        advanceTimeBy(2998)
        runCurrent()
        viewModel.state.value.showDraftSaved shouldBe true

        advanceTimeBy(2)
        runCurrent()
        viewModel.state.value.showDraftSaved shouldBe false
    }

    @Test
    fun `離開再打開帶回草稿`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val first = viewModelWith(useCases)
        act(
            first,
            ItineraryAddEditIntent.LoadDraft,
            ItineraryAddEditIntent.UpdateTitle("京都大阪 5 日遊"),
            ItineraryAddEditIntent.UpdateDescription("秋天賞楓"),
            ItineraryAddEditIntent.UpdateStartDate(LocalDate(2026, 11, 20)),
            ItineraryAddEditIntent.UpdateEndDate(LocalDate(2026, 11, 24)),
        )

        val second = viewModelWith(useCases)
        act(second, ItineraryAddEditIntent.LoadDraft)

        second.state.value.title shouldBe "京都大阪 5 日遊"
        second.state.value.description shouldBe "秋天賞楓"
        second.state.value.startDate shouldBe LocalDate(2026, 11, 20)
        second.state.value.endDate shouldBe LocalDate(2026, 11, 24)
    }

    @Test
    fun `建立成功後清掉草稿`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val first = viewModelWith(useCases)
        act(
            first,
            ItineraryAddEditIntent.LoadDraft,
            ItineraryAddEditIntent.UpdateTitle("京都大阪 5 日遊"),
            ItineraryAddEditIntent.Save,
        )

        val second = viewModelWith(useCases)
        act(second, ItineraryAddEditIntent.LoadDraft)

        second.state.value.title shouldBe ""
    }

    @Test
    fun `打字後馬上建立`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val first = viewModelWith(useCases)
        act(first, ItineraryAddEditIntent.LoadDraft)
        type(first, "京都大阪 5 日遊")
        act(first, ItineraryAddEditIntent.Save)

        val second = viewModelWith(useCases)
        act(second, ItineraryAddEditIntent.LoadDraft)

        second.state.value.title shouldBe ""
    }

    @Test
    fun `編輯行程時不存草稿`() = runTest(dispatcher) {
        val drafts = CountingDraftRepository(inMemoryDraftRepository())
        val viewModel = viewModelWith(useCasesOf(drafts = drafts))

        act(viewModel, ItineraryAddEditIntent.LoadItinerary("kyoto"), ItineraryAddEditIntent.UpdateTitle("京都大阪 6 日遊"))

        drafts.saveCount shouldBe 0
    }

    // ── 捨棄：清掉草稿再離開 ──

    @Test
    fun `按捨棄後清掉草稿`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val first = viewModelWith(useCases)
        val events = eventsOf(first)
        act(first, ItineraryAddEditIntent.LoadDraft, ItineraryAddEditIntent.UpdateTitle("京都大阪 5 日遊"))
        useCases.loadDraft(DraftType.ITINERARY).getOrThrow()?.get("title") shouldBe "京都大阪 5 日遊"

        act(first, ItineraryAddEditIntent.Discard)

        events.last() shouldBe ItineraryAddEditEvent.NavigateBack
        val second = viewModelWith(useCases)
        act(second, ItineraryAddEditIntent.LoadDraft)
        second.state.value.title shouldBe ""
    }

    @Test
    fun `打字後馬上捨棄`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        val first = viewModelWith(useCases)
        val events = eventsOf(first)
        act(first, ItineraryAddEditIntent.LoadDraft)
        type(first, "京都大阪 5 日遊")

        act(first, ItineraryAddEditIntent.Discard)

        events.last() shouldBe ItineraryAddEditEvent.NavigateBack
        val second = viewModelWith(useCases)
        act(second, ItineraryAddEditIntent.LoadDraft)
        second.state.value.title shouldBe ""
    }

    @Test
    fun `編輯行程時捨棄不動新增行程的草稿`() = runTest(dispatcher) {
        val useCases = useCasesOf()
        act(viewModelWith(useCases), ItineraryAddEditIntent.LoadDraft, ItineraryAddEditIntent.UpdateTitle("京都大阪 5 日遊"))
        val editing = viewModelWith(useCases)
        val events = eventsOf(editing)

        act(editing, ItineraryAddEditIntent.LoadItinerary("kyoto"), ItineraryAddEditIntent.Discard)

        events.last() shouldBe ItineraryAddEditEvent.NavigateBack
        useCases.loadDraft(DraftType.ITINERARY).getOrThrow()?.get("title") shouldBe "京都大阪 5 日遊"
    }

    // ── 測試工具 ──

    private val photoBytes = byteArrayOf(1, 2, 3)

    private fun useCasesOf(
        wrap: (ItineraryRepository) -> ItineraryRepository = { it },
        drafts: DraftRepository = inMemoryDraftRepository(),
    ): TestUseCases {
        val itemRepository = InMemoryItineraryItemRepository()
        val itineraryRepository = wrap(InMemoryItineraryRepository(itemRepository, kyoto))
        return TestUseCases(itineraryRepository, itemRepository, imageStorage = imageStorage, drafts = drafts)
    }

    /** 輸入標題，只跑到指令開始執行，不推進時間 */
    private fun TestScope.type(viewModel: ItineraryAddEditViewModel, title: String) {
        viewModel.processIntent(ItineraryAddEditIntent.UpdateTitle(title))
        runCurrent()
    }

    private fun viewModelWith(useCases: TestUseCases) = ItineraryAddEditViewModel(
        createItineraryUseCase = useCases.createItinerary,
        updateItineraryUseCase = useCases.updateItinerary,
        getItineraryUseCase = useCases.getItinerary,
        saveDraftUseCase = useCases.saveDraft,
        loadDraftUseCase = useCases.loadDraft,
        deleteDraftUseCase = useCases.deleteDraft,
        imageStorageService = useCases.imageStorage,
    )

    /** 從現在起收集 [viewModel] 發出的事件 */
    private fun TestScope.eventsOf(viewModel: ItineraryAddEditViewModel): List<ItineraryAddEditEvent> {
        val events = mutableListOf<ItineraryAddEditEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events += it } }
        return events
    }

    /** 依序送出指令，每送一個就把排程跑完 */
    private fun TestScope.act(viewModel: ItineraryAddEditViewModel, vararg intents: ItineraryAddEditIntent) {
        intents.forEach {
            viewModel.processIntent(it)
            advanceUntilIdle()
        }
    }
}
