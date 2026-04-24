package com.example.myapplication

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.myapplication.core.presentation.navigation.AppRoute
import com.example.myapplication.core.presentation.theme.Spacing
import com.example.myapplication.core.presentation.theme.TravelRecordTheme
import com.example.myapplication.core.presentation.util.shareLauncher
import com.example.myapplication.feature.itinerary_add_edit.presentation.ItineraryAddEditScreen
import com.example.myapplication.feature.itinerary_detail.presentation.ItineraryDetailScreen
import com.example.myapplication.feature.itinerary_detail.presentation.ItineraryDetailViewModel
import com.example.myapplication.feature.itinerary_item.presentation.AddEditItemScreen
import com.example.myapplication.feature.itinerary_item.presentation.EditItemScreen
import com.example.myapplication.feature.itinerary_list.presentation.ItineraryListScreen
import com.example.myapplication.feature.itinerary_list.presentation.ItineraryListViewModel
import com.example.myapplication.feature.footprint.presentation.FootprintScreen
import com.example.myapplication.feature.itinerary_map.presentation.ItineraryMapScreen
import com.example.myapplication.feature.itinerary_map.presentation.ItineraryMapViewModel
import com.example.myapplication.feature.memory.presentation.MemoryScreen
import com.example.myapplication.feature.route.presentation.RouteViewScreen
import com.example.myapplication.feature.today.presentation.TodayScreen
import kotlin.time.ExperimentalTime
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

private enum class AppTab(
    val label: String
) {
    Plan("行程"),
    Today("今日"),
    Footprints("足跡"),
    Memories("回憶")
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
    val planBackStack = remember { mutableStateListOf<Any>(AppRoute.ItineraryList) }
    var selectedTab by remember { mutableStateOf(AppTab.Plan) }
    val snackbarHostState = remember { SnackbarHostState() }
    val shareLauncher = shareLauncher()

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                TravelBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        ) { paddingValues ->
            when (selectedTab) {
                AppTab.Plan -> PlanNavHost(
                    backStack = planBackStack,
                    modifier = Modifier.padding(paddingValues),
                    onSwitchToFootprints = { selectedTab = AppTab.Footprints },
                    onShareRoute = { json -> shareLauncher.share(json, "分享行程路線") }
                )

                AppTab.Today -> TodayScreen(
                    modifier = Modifier.padding(paddingValues),
                    onOpenCurrentTrip = { itineraryId ->
                        while (planBackStack.size > 1) {
                            planBackStack.removeLastOrNull()
                        }
                        planBackStack.add(AppRoute.ItineraryDetail(itineraryId))
                        selectedTab = AppTab.Plan
                    },
                    onGoToPlan = { selectedTab = AppTab.Plan }
                )

                AppTab.Footprints -> FootprintScreen(
                    modifier = Modifier.padding(paddingValues)
                )

                AppTab.Memories -> MemoryScreen(
                    modifier = Modifier.padding(paddingValues)
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

