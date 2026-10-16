package io.github.ppking2897.travelrecord

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.ppking2897.travelrecord.core.presentation.navigation.AppRoute
import io.github.ppking2897.travelrecord.core.presentation.navigation.AppStartViewModel
import io.github.ppking2897.travelrecord.core.presentation.navigation.AppTab
import io.github.ppking2897.travelrecord.core.presentation.theme.Spacing
import io.github.ppking2897.travelrecord.core.presentation.theme.TravelRecordTheme
import io.github.ppking2897.travelrecord.core.presentation.util.shareLauncher
import io.github.ppking2897.travelrecord.feature.itinerary_add_edit.presentation.ItineraryAddEditScreen
import io.github.ppking2897.travelrecord.feature.itinerary_detail.presentation.ItineraryDetailScreen
import io.github.ppking2897.travelrecord.feature.itinerary_detail.presentation.ItineraryDetailViewModel
import io.github.ppking2897.travelrecord.feature.itinerary_item.presentation.AddEditItemScreen
import io.github.ppking2897.travelrecord.feature.itinerary_item.presentation.EditItemScreen
import io.github.ppking2897.travelrecord.feature.itinerary_list.presentation.ItineraryListScreen
import io.github.ppking2897.travelrecord.feature.itinerary_list.presentation.ItineraryListViewModel
import io.github.ppking2897.travelrecord.feature.footprint.presentation.FootprintScreen
import io.github.ppking2897.travelrecord.feature.itinerary_map.presentation.ItineraryMapScreen
import io.github.ppking2897.travelrecord.feature.itinerary_map.presentation.ItineraryMapViewModel
import io.github.ppking2897.travelrecord.feature.memory.presentation.MemoryScreen
import io.github.ppking2897.travelrecord.feature.route.presentation.RouteViewScreen
import io.github.ppking2897.travelrecord.feature.today.presentation.TodayScreen
import kotlin.time.ExperimentalTime
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

@ExperimentalTime
@Composable
@Preview
fun App() {
    KoinContext { TravelRecordTheme { TravelApp() } }
}

@ExperimentalTime
@Composable
fun TravelApp() {
    val appStartViewModel: AppStartViewModel = koinViewModel()
    val appStartState by appStartViewModel.state.collectAsState()
    val planBackStack = remember { mutableStateListOf<Any>(AppRoute.ItineraryList) }
    // null = 還在判斷啟動分頁；rememberSaveable 讓旋轉螢幕後留在使用者所在的分頁
    var selectedTab by rememberSaveable { mutableStateOf<AppTab?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val shareLauncher = shareLauncher()

    LaunchedEffect(appStartState.startTab) {
        if (selectedTab == null) {
            selectedTab = appStartState.startTab
        }
    }

    // 從其他分頁打開某趟行程：行程分頁回到列表後推入詳情，返回時回到列表
    val openTripDetail: (String) -> Unit = { itineraryId ->
        while (planBackStack.size > 1) {
            planBackStack.removeLastOrNull()
        }
        planBackStack.add(AppRoute.ItineraryDetail(itineraryId))
        selectedTab = AppTab.Plan
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                val currentTab = selectedTab
                val isPlanDeepLevel = currentTab == AppTab.Plan && planBackStack.size > 1
                if (currentTab != null && !isPlanDeepLevel) {
                    TravelBottomNavigation(
                        selectedTab = currentTab,
                        onTabSelected = { selectedTab = it }
                    )
                }
            }
        ) { paddingValues ->
            // 外框已讓出狀態列與底部分頁列的空間，標記為已處理，各頁的標題列與內容就不會再讓一次
            val contentModifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues)
            when (selectedTab) {
                null -> Unit

                AppTab.Plan -> PlanNavHost(
                    backStack = planBackStack,
                    modifier = contentModifier,
                    onSwitchToFootprints = { selectedTab = AppTab.Footprints },
                    onShareRoute = { json -> shareLauncher.share(json, "分享行程路線") }
                )

                AppTab.Today -> TodayScreen(
                    modifier = contentModifier,
                    onOpenCurrentTrip = openTripDetail,
                    onGoToPlan = { selectedTab = AppTab.Plan }
                )

                AppTab.Footprints -> FootprintScreen(
                    modifier = contentModifier,
                    onOpenTrip = openTripDetail
                )

                AppTab.Memories -> MemoryScreen(
                    modifier = contentModifier,
                    onOpenTrip = openTripDetail
                )
            }
        }
    }
}

@Composable
private fun TravelBottomNavigation(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    NavigationBar {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            AppTab.Plan -> Icons.Default.DateRange
                            AppTab.Today -> Icons.Default.Add
                            AppTab.Footprints -> Icons.Default.Map
                            AppTab.Memories -> Icons.Default.Star
                        },
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) }
            )
        }
    }
}

@ExperimentalTime
@Composable
private fun PlanNavHost(
    backStack: MutableList<Any>,
    modifier: Modifier = Modifier,
    onSwitchToFootprints: () -> Unit,
    onShareRoute: (String) -> Unit
) {
    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
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
                is AppRoute.ItineraryList -> NavEntry(key) {
                    val viewModel: ItineraryListViewModel = koinViewModel()
                    ItineraryListScreen(
                        viewModel = viewModel,
                        onItineraryClick = { id -> backStack.add(AppRoute.ItineraryDetail(id)) },
                        onEditClick = { id -> backStack.add(AppRoute.EditItinerary(id)) },
                        onAddClick = { backStack.add(AppRoute.AddItinerary) },
                        onHistoryClick = onSwitchToFootprints
                    )
                }

                is AppRoute.AddItinerary -> NavEntry(key) {
                    ItineraryAddEditScreen(
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = { id ->
                            while (backStack.size > 1) backStack.removeLastOrNull()
                            backStack.add(AppRoute.ItineraryDetail(id))
                        }
                    )
                }

                is AppRoute.EditItinerary -> NavEntry(key) {
                    ItineraryAddEditScreen(
                        itineraryId = key.itineraryId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = { backStack.removeLastOrNull() }
                    )
                }

                is AppRoute.ItineraryDetail -> NavEntry(key) {
                    val viewModel: ItineraryDetailViewModel = koinViewModel()
                    ItineraryDetailScreen(
                        viewModel = viewModel,
                        itineraryId = key.itineraryId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onAddItemClick = { backStack.add(AppRoute.AddItem(key.itineraryId)) },
                        onQuickAddItemClick = { defaultDate ->
                            backStack.add(AppRoute.QuickAddItem(key.itineraryId, defaultDate.toString()))
                        },
                        onEditItemClick = { itemId -> backStack.add(AppRoute.EditItem(itemId)) },
                        onEditItineraryClick = { backStack.add(AppRoute.EditItinerary(key.itineraryId)) },
                        onRouteClick = { routeId -> backStack.add(AppRoute.RouteView(routeId)) },
                        onMapClick = { backStack.add(AppRoute.ItineraryMap(key.itineraryId)) }
                    )
                }

                is AppRoute.AddItem -> NavEntry(key) {
                    AddEditItemScreen(
                        itineraryId = key.itineraryId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = { backStack.removeLastOrNull() },
                        onNavigateToEditItinerary = {
                            backStack.removeLastOrNull()
                            backStack.add(AppRoute.EditItinerary(key.itineraryId))
                        }
                    )
                }

                is AppRoute.QuickAddItem -> NavEntry(key) {
                    val defaultDate = kotlinx.datetime.LocalDate.parse(key.defaultDate)
                    AddEditItemScreen(
                        itineraryId = key.itineraryId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = { backStack.removeLastOrNull() },
                        onNavigateToEditItinerary = {
                            backStack.removeLastOrNull()
                            backStack.add(AppRoute.EditItinerary(key.itineraryId))
                        },
                        defaultDate = defaultDate
                    )
                }

                is AppRoute.EditItem -> NavEntry(key) {
                    EditItemScreen(
                        itemId = key.itemId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onSaveSuccess = { backStack.removeLastOrNull() }
                    )
                }

                is AppRoute.RouteView -> NavEntry(key) {
                    RouteViewScreen(
                        routeId = key.routeId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onExportSuccess = onShareRoute
                    )
                }

                is AppRoute.ItineraryMap -> NavEntry(key) {
                    val viewModel: ItineraryMapViewModel = koinViewModel()
                    ItineraryMapScreen(
                        viewModel = viewModel,
                        itineraryId = key.itineraryId,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        onEditItemClick = { itemId -> backStack.add(AppRoute.EditItem(itemId)) }
                    )
                }

                else -> NavEntry(Unit) {
                    Text("Unknown route")
                }
            }
        }
    )
}

