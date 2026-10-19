@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package io.github.ppking2897.travelrecord.feature.itinerary_item.presentation

import io.github.ppking2897.travelrecord.test.FailingSaveImageStorageService
import io.github.ppking2897.travelrecord.test.InMemoryItineraryItemRepository
import io.github.ppking2897.travelrecord.test.InMemoryItineraryRepository
import io.github.ppking2897.travelrecord.test.TestUseCases
import io.github.ppking2897.travelrecord.test.testItem
import io.github.ppking2897.travelrecord.test.testItinerary
import io.github.ppking2897.travelrecord.test.testPhoto
import io.kotest.matchers.collections.shouldBeEmpty
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.ExperimentalTime

/**
 * 新增、編輯項目時照片存檔失敗要提示，測試名稱對應 A 組修正討論時定下的情境
 */
class ItemPhotoSaveFailureTest {

    private val dispatcher = StandardTestDispatcher()

    private val kyoto = testItinerary("kyoto", title = "京都大阪 5 日遊")
    private val kinkaku = testItem("kinkaku", "kyoto", locationName = "金閣寺")
        .copy(photos = listOf(testPhoto("p1", "kinkaku")))

    private val useCases = run {
        val itemRepository = InMemoryItineraryItemRepository(kinkaku)
        TestUseCases(
            InMemoryItineraryRepository(itemRepository, kyoto),
            itemRepository,
            imageStorage = FailingSaveImageStorageService(),
        )
    }

    private val photoBytes = byteArrayOf(1, 2, 3)

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `新增項目時照片存檔失敗`() = runTest(dispatcher) {
        val viewModel = AddEditItemViewModel(useCases.addItem, useCases.getItinerary, useCases.imageStorage)
        val events = mutableListOf<AddEditItemEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events += it } }
        act { viewModel.processIntent(AddEditItemIntent.LoadItinerary("kyoto")) }

        act { viewModel.processIntent(AddEditItemIntent.AddPhotoByContent(photoBytes)) }

        events shouldBe listOf(AddEditItemEvent.ShowError("照片加入失敗"))
        viewModel.state.value.photos.shouldBeEmpty()
    }

    @Test
    fun `編輯項目時照片存檔失敗`() = runTest(dispatcher) {
        val viewModel = EditItemViewModel(
            useCases.updateItem,
            useCases.getItem,
            useCases.getItinerary,
            useCases.imageStorage,
        )
        val events = mutableListOf<EditItemEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.collect { events += it } }
        act { viewModel.processIntent(EditItemIntent.LoadItem("kinkaku")) }
        val photosBefore = viewModel.state.value.photos

        act { viewModel.processIntent(EditItemIntent.AddPhotoByContent(photoBytes)) }

        events shouldBe listOf(EditItemEvent.ShowError("照片加入失敗"))
        viewModel.state.value.photos shouldBe photosBefore
    }

    /** 送出指令後把排程跑完 */
    private fun TestScope.act(send: () -> Unit) {
        send()
        advanceUntilIdle()
    }
}
