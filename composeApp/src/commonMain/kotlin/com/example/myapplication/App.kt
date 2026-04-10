package com.example.myapplication

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.core.presentation.navigation.AppRoute
import com.example.myapplication.core.presentation.theme.TravelRecordTheme
import com.example.myapplication.feature.itinerary_list.presentation.ItineraryListScreen
import com.example.myapplication.feature.itinerary_list.presentation.ItineraryListViewModel
import com.example.myapplication.feature.itinerary_detail.presentation.ItineraryDetailScreen
import com.example.myapplication.feature.itinerary_detail.presentation.ItineraryDetailViewModel
import com.example.myapplication.feature.itinerary_add_edit.presentation.ItineraryAddEditScreen
import com.example.myapplication.feature.itinerary_item.presentation.AddEditItemScreen
import com.example.myapplication.feature.itinerary_item.presentation.EditItemScreen
import com.example.myapplication.feature.travel_history.presentation.TravelHistoryScreen
import com.example.myapplication.feature.travel_history.presentation.TravelHistoryViewModel
import com.example.myapplication.feature.route.presentation.RouteViewScreen
import com.example.myapplication.feature.itinerary_map.presentation.ItineraryMapScreen
import com.example.myapplication.feature.itinerary_map.presentation.ItineraryMapViewModel
import kotlin.time.ExperimentalTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

/**
 * 旅遊流程記事應用程式主入口
 */
@ExperimentalTime
@Composable
@Preview
fun App() {
    KoinContext { TravelRecordTheme { TravelApp() } }
}

@ExperimentalTime
@Composable
fun TravelApp() {
    val backStack = remember { mutableStateListOf<Any>(AppRoute.ItineraryList) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                slideOutHorizontally(targetOffsetX = { it })
        },
        entryProvider = { key ->
            when (key) {
                // 行程列表
                is AppRoute.ItineraryList -> NavEntry(key) {
                    val viewModel: ItineraryListViewModel = koinViewModel()
                    ItineraryListScreen(
                        viewModel = viewModel,
                        onItineraryClick = { id ->
                            backStack.add(AppRoute.ItineraryDetail(id))
                        },
                        onEditClick = { id ->
                            backStack.add(AppRoute.EditItinerary(id))
                        },
                        onAddClick = { backStack.add(AppRoute.AddItinerary) },
                        onHistoryClick = { backStack.add(AppRoute.TravelHistory) }
                    )
                }

                // 新增行程
                is AppRoute.AddItinerary -> NavEntry(key) {
                    ItineraryAddEditScreen(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = { id ->
                            while (backStack.size > 1) backStack.removeLastOrNull()
                            backStack.add(AppRoute.ItineraryDetail(id))
                        }
                    )
                }

                // 編輯行程
                is AppRoute.EditItinerary -> NavEntry(key) {
                    ItineraryAddEditScreen(
                        itineraryId = key.itineraryId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = { _ -> backStack.removeLastOrNull() }
                    )
                }

                // 行程詳情
                is AppRoute.ItineraryDetail -> NavEntry(key) {
                    val viewModel: ItineraryDetailViewModel = koinViewModel()
                    ItineraryDetailScreen(
                        viewModel = viewModel,
                        itineraryId = key.itineraryId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onAddItemClick = {
                            backStack.add(AppRoute.AddItem(key.itineraryId))
                        },
                        onQuickAddItemClick = { defaultDate ->
                            backStack.add(AppRoute.QuickAddItem(
                                key.itineraryId,
                                defaultDate.toString()
                            ))
                        },
                        onEditItemClick = { itemId ->
                            backStack.add(AppRoute.EditItem(itemId))
                        },
                        onEditItineraryClick = {
                            backStack.add(AppRoute.EditItinerary(key.itineraryId))
                        },
                        onDeleteItineraryClick = {},
                        onGenerateRouteClick = {},
                        onMapClick = {
                            backStack.add(AppRoute.ItineraryMap(key.itineraryId))
                        }
                    )
                }

                // 新增項目
                is AppRoute.AddItem -> NavEntry(key) {
                    AddEditItemScreen(
                        itineraryId = key.itineraryId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = { backStack.removeLastOrNull() }
                    )
                }

                // 快速新增項目
                is AppRoute.QuickAddItem -> NavEntry(key) {
                    val defaultDate = kotlinx.datetime.LocalDate.parse(key.defaultDate)
                    AddEditItemScreen(
                        itineraryId = key.itineraryId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = { backStack.removeLastOrNull() },
                        defaultDate = defaultDate
                    )
                }

                // 編輯項目
                is AppRoute.EditItem -> NavEntry(key) {
                    EditItemScreen(
                        itemId = key.itemId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = { backStack.removeLastOrNull() }
                    )
                }

                // 旅遊歷史
                is AppRoute.TravelHistory -> NavEntry(key) {
                    val viewModel: TravelHistoryViewModel = koinViewModel()
                    TravelHistoryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { backStack.removeLastOrNull() }
                    )
                }

                // 路線檢視
                is AppRoute.RouteView -> NavEntry(key) {
                    RouteViewScreen(
                        routeId = key.routeId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onExportSuccess = { json -> println("Export JSON: $json") }
                    )
                }

                // 行程地圖
                is AppRoute.ItineraryMap -> NavEntry(key) {
                    val viewModel: ItineraryMapViewModel = koinViewModel()
                    ItineraryMapScreen(
                        viewModel = viewModel,
                        itineraryId = key.itineraryId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onEditItemClick = { itemId ->
                            backStack.add(AppRoute.EditItem(itemId))
                        }
                    )
                }

                else -> NavEntry(Unit) {
                    androidx.compose.material3.Text("Unknown route")
                }
            }
        }
    )
}
