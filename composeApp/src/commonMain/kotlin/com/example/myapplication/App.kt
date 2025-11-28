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
import kotlinx.coroutines.launch
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
            ItineraryListScreen(
                onItineraryClick = { id ->
                    navController.navigate(Screen.ItineraryDetail.createRoute(id))
                },
                onEditClick = { id ->
                    navController.navigate(Screen.EditItinerary.createRoute(id))
                },
                onAddClick = {
                    navController.navigate(Screen.AddItinerary.route)
                }
            )
        }
        
        // 新增行程
        composable(Screen.AddItinerary.route) {
            val createUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.CreateItineraryUseCase>()
            val saveDraftUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.SaveDraftUseCase>()
            val loadDraftUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.LoadDraftUseCase>()
            val draftRepository = org.koin.compose.koinInject<com.example.myapplication.data.repository.DraftRepository>()
            
            AddEditItineraryScreen(
                createItineraryUseCase = createUseCase,
                saveDraftUseCase = saveDraftUseCase,
                loadDraftUseCase = loadDraftUseCase,
                draftRepository = draftRepository,
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
            val createUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.CreateItineraryUseCase>()
            val updateUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.UpdateItineraryUseCase>()
            val itineraryRepository = org.koin.compose.koinInject<com.example.myapplication.data.repository.ItineraryRepository>()
            val saveDraftUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.SaveDraftUseCase>()
            val loadDraftUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.LoadDraftUseCase>()
            val draftRepository = org.koin.compose.koinInject<com.example.myapplication.data.repository.DraftRepository>()
            
            AddEditItineraryScreen(
                itineraryId = itineraryId,
                createItineraryUseCase = createUseCase,
                updateItineraryUseCase = updateUseCase,
                itineraryRepository = itineraryRepository,
                saveDraftUseCase = saveDraftUseCase,
                loadDraftUseCase = loadDraftUseCase,
                draftRepository = draftRepository,
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
            val deleteUseCase = org.koin.compose.koinInject<com.example.myapplication.domain.usecase.DeleteItineraryUseCase>()
            
            var showDeleteDialog by remember { mutableStateOf(false) }
            var itineraryTitle by remember { mutableStateOf("") }
            
            LaunchedEffect(itineraryId) {
                viewModel.itinerary.collect { itinerary ->
                    itinerary?.let { itineraryTitle = it.title }
                }
            }
            
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
                onEditItineraryClick = {
                    navController.navigate(Screen.EditItinerary.createRoute(itineraryId))
                },
                onDeleteItineraryClick = {
                    showDeleteDialog = true
                },
                onGenerateRouteClick = {
                    // TODO: 實作路線生成並導航到 RouteView
                }
            )
            
            // 刪除行程確認 Dialog
            if (showDeleteDialog) {
                com.example.myapplication.ui.component.DeleteConfirmDialog(
                    title = "確認刪除",
                    message = "確定要刪除「$itineraryTitle」嗎？此操作無法復原，所有項目也會被刪除。",
                    onConfirm = {
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            deleteUseCase(itineraryId).onSuccess {
                                navController.popBackStack()
                            }
                        }
                        showDeleteDialog = false
                    },
                    onDismiss = {
                        showDeleteDialog = false
                    }
                )
            }
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
            TravelHistoryScreen(
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
                    // TODO: 實作匯出功能（分享、儲存等）
                    println("Export JSON: $json")
                }
            )
        }
    }
}