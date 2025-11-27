package com.example.myapplication

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.di.appModule
import com.example.myapplication.ui.navigation.Screen
import com.example.myapplication.ui.screen.*
import com.example.myapplication.ui.viewmodel.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.ExperimentalTime

/**
 * 旅遊流程記事應用程式主入口
 * 
 * 已整合：
 * - Koin 依賴注入
 * - Navigation Compose
 * - 所有 ViewModels
 * - 所有 UI Screens
 */
@ExperimentalTime
@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        MaterialTheme {
            TravelApp()
        }
    }
}

@ExperimentalTime
@Composable
fun TravelApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.ItineraryList.route
    ) {
        // 行程列表
        composable(Screen.ItineraryList.route) {
            val viewModel: ItineraryListViewModel = koinViewModel()
            ItineraryListScreen(
                viewModel = viewModel,
                onItineraryClick = { id ->
                    navController.navigate(Screen.ItineraryDetail.createRoute(id))
                },
                onAddClick = {
                    navController.navigate(Screen.AddItinerary.route)
                }
            )
        }
        
        // 新增行程
        composable(Screen.AddItinerary.route) {
            val createUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.CreateItineraryUseCase>()
            AddEditItineraryScreen(
                createItineraryUseCase = createUseCase,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { id ->
                    navController.navigate(Screen.ItineraryDetail.createRoute(id)) {
                        popUpTo(Screen.ItineraryList.route)
                    }
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
                onEditItemClick = { itemId ->
                    navController.navigate(Screen.EditItem.createRoute(itemId))
                },
                onGenerateRouteClick = {
                    // TODO: 實作路線生成並導航到 RouteView
                }
            )
        }
        
        // 新增項目
        composable(
            route = Screen.AddItem.route,
            arguments = listOf(navArgument("itineraryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itineraryId = backStackEntry.arguments?.getString("itineraryId") ?: return@composable
            val addItemUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.AddItineraryItemUseCase>()
            val itineraryRepository = org.koin.compose.koinInject<com.example.myapplication.data.repository.ItineraryRepository>()
            
            var itinerary by remember { mutableStateOf<com.example.myapplication.data.model.Itinerary?>(null) }
            LaunchedEffect(itineraryId) {
                itineraryRepository.getItinerary(itineraryId).onSuccess {
                    itinerary = it
                }
            }
            
            itinerary?.let {
                AddEditItemScreen(
                    itinerary = it,
                    addItemUseCase = addItemUseCase,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
        }
        
        // 編輯項目
        composable(
            route = Screen.EditItem.route,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            val updateItemUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.UpdateItineraryItemUseCase>()
            val itemRepository = org.koin.compose.koinInject<com.example.myapplication.data.repository.ItineraryItemRepository>()
            val itineraryRepository = org.koin.compose.koinInject<com.example.myapplication.data.repository.ItineraryRepository>()
            
            var item by remember { mutableStateOf<com.example.myapplication.data.model.ItineraryItem?>(null) }
            var itinerary by remember { mutableStateOf<com.example.myapplication.data.model.Itinerary?>(null) }
            
            LaunchedEffect(itemId) {
                itemRepository.getItem(itemId).onSuccess { loadedItem ->
                    item = loadedItem
                    loadedItem?.let {
                        itineraryRepository.getItinerary(it.itineraryId).onSuccess { loadedItinerary ->
                            itinerary = loadedItinerary
                        }
                    }
                }
            }
            
            if (item != null && itinerary != null) {
                EditItemScreen(
                    item = item!!,
                    itinerary = itinerary!!,
                    updateItemUseCase = updateItemUseCase,
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
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
            val routeRepository = org.koin.compose.koinInject<com.example.myapplication.data.repository.RouteRepository>()
            RouteViewScreen(
                routeId = routeId,
                routeRepository = routeRepository,
                onNavigateBack = { navController.popBackStack() },
                onExportClick = { json ->
                    // TODO: 實作匯出功能（分享、儲存等）
                    println("Export JSON: $json")
                }
            )
        }
    }
}