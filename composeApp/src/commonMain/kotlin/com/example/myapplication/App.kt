package com.example.myapplication

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.core.presentation.navigation.Screen
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
import com.example.myapplication.feature.itinerary.presentation.ItineraryMapScreen
import com.example.myapplication.feature.itinerary.presentation.ItineraryMapViewModel
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
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.ItineraryList.route) {
        // 行程列表
        composable(Screen.ItineraryList.route) {
            val viewModel: ItineraryListViewModel = koinViewModel()
            ItineraryListScreen(
                viewModel = viewModel,
                onItineraryClick = { id ->
                    navController.navigate(Screen.ItineraryDetail.createRoute(id))
                },
                onEditClick = { id ->
                    navController.navigate(Screen.EditItinerary.createRoute(id))
                },
                onAddClick = { navController.navigate(Screen.AddItinerary.route) },
                onHistoryClick = { navController.navigate(Screen.TravelHistory.route) }
            )
        }

        // 新增行程
        composable(Screen.AddItinerary.route) {
            ItineraryAddEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { id ->
                    navController.navigate(Screen.ItineraryDetail.createRoute(id)) {
                        popUpTo(Screen.ItineraryList.route)
                    }
                }
            )
        }

        // 編輯行程
        composable(
            route = Screen.EditItinerary.route,
            arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: return@composable
            ItineraryAddEditScreen(
                itineraryId = itineraryId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { _ ->
                    navController.popBackStack()
                }
            )
        }

        // 行程詳情
        composable(
            route = Screen.ItineraryDetail.route,
            arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: return@composable
            val viewModel: ItineraryDetailViewModel = koinViewModel()

            ItineraryDetailScreen(
                viewModel = viewModel,
                itineraryId = itineraryId,
                onNavigateBack = { navController.popBackStack() },
                onAddItemClick = {
                    navController.navigate(Screen.AddItem.createRoute(itineraryId))
                },
                onQuickAddItemClick = { defaultDate ->
                    navController.navigate(Screen.QuickAddItem.createRoute(itineraryId, defaultDate.toString()))
                },
                onEditItemClick = { itemId ->
                    navController.navigate(Screen.EditItem.createRoute(itemId))
                },
                onEditItineraryClick = {
                    navController.navigate(Screen.EditItinerary.createRoute(itineraryId))
                },
                onDeleteItineraryClick = {},
                onGenerateRouteClick = {},
                onMapClick = {
                    navController.navigate(Screen.ItineraryMap.createRoute(itineraryId))
                }
            )
        }

        // 新增項目
        composable(
            route = Screen.AddItem.route,
            arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: return@composable
            AddEditItemScreen(
                itineraryId = itineraryId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // 快速新增項目（帶預設日期）
        composable(
            route = Screen.QuickAddItem.route,
            arguments = listOf(
                navArgument("itineraryId") { type = NavType.StringType },
                navArgument("defaultDate") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: return@composable
            val defaultDateString = backStackEntry.arguments?.getString("defaultDate") ?: return@composable
            val defaultDate = kotlinx.datetime.LocalDate.parse(defaultDateString)
            AddEditItemScreen(
                itineraryId = itineraryId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() },
                defaultDate = defaultDate
            )
        }

        // 編輯項目
        composable(
            route = Screen.EditItem.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            EditItemScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // 旅遊歷史
        composable(Screen.TravelHistory.route) {
            val viewModel: TravelHistoryViewModel = koinViewModel()
            TravelHistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 路線檢視
        composable(
            route = Screen.RouteView.route,
            arguments = listOf(navArgument("routeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routeId = backStackEntry.arguments?.getString("routeId") ?: return@composable
            RouteViewScreen(
                routeId = routeId,
                onNavigateBack = { navController.popBackStack() },
                onExportSuccess = { json ->
                    println("Export JSON: $json")
                }
            )
        }

        // 行程地圖
        composable(
            route = Screen.ItineraryMap.route,
            arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: return@composable
            val viewModel: ItineraryMapViewModel = koinViewModel()
            ItineraryMapScreen(
                viewModel = viewModel,
                itineraryId = itineraryId,
                onNavigateBack = { navController.popBackStack() },
                onEditItemClick = { itemId ->
                    navController.navigate(Screen.EditItem.createRoute(itemId))
                }
            )
        }
    }
}
