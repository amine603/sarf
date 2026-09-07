package com.cash.guide.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cash.guide.feature.editor.CalculationEditorScreen
import com.cash.guide.feature.editor.CalculationEditorViewModel
import com.cash.guide.feature.history.HistoryScreen
import com.cash.guide.feature.history.HistoryViewModel
import com.cash.guide.feature.home.HomeScreen
import com.cash.guide.feature.home.HomeViewModel
import com.cash.guide.feature.settings.SettingsScreen
import com.cash.guide.feature.settings.SettingsViewModel

import com.cash.guide.feature.groups.GroupsScreen
import com.cash.guide.feature.groups.GroupsViewModel
import com.cash.guide.feature.groups.GroupDetailScreen
import com.cash.guide.feature.groups.GroupDetailViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.SettingsRepository
import com.cash.guide.feature.history.MonthCalculationsScreen
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun HssabiNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    groupsViewModel: GroupsViewModel,
    historyViewModel: HistoryViewModel,
    settingsViewModel: SettingsViewModel,
    calculationRepository: CalculationRepository,
    settingsRepository: SettingsRepository,
    editorViewModelFactory: () -> CalculationEditorViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Home.route,
        modifier = modifier
    ) {
        composable(AppDestination.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNewCalculation = { navController.navigate(AppDestination.NewCalculation.route) },
                onOpenCalculation = { id -> navController.navigate("calculation/$id") },
                onOpenHistory = { navController.navigate(AppDestination.History.route) },
                onOpenMonthCalculations = { year, month ->
                    navController.navigate("month_calculations/$year/$month")
                },
                onOpenStyleShowcase = { navController.navigate(AppDestination.StyleShowcase.route) }
            )
        }

        composable(AppDestination.Groups.route) {
            GroupsScreen(
                viewModel = groupsViewModel,
                settingsRepository = settingsRepository,
                onOpenGroup = { groupId -> navController.navigate("group/$groupId") }
            )
        }

        composable(
            route = AppDestination.GroupDetail.ROUTE_PATTERN,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            val groupDetailViewModel: GroupDetailViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                key = "group_detail_$groupId"
            ) {
                GroupDetailViewModel(groupId, calculationRepository)
            }
            GroupDetailScreen(
                viewModel = groupDetailViewModel,
                settingsRepository = settingsRepository,
                onBack = { navController.popBackStack() },
                onOpenCalculation = { id -> navController.navigate("calculation/$id") },
                onNewCalculationInGroup = { gid ->
                    coroutineScope.launch {
                        calculationRepository.createDraftInGroup(gid)
                        navController.navigate(AppDestination.NewCalculation.routeForGroup(gid))
                    }
                }
            )
        }

        composable(AppDestination.StyleShowcase.route) {
            com.cash.guide.feature.showcase.TestNotebookShowcaseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.History.route) {
            HistoryScreen(
                viewModel = historyViewModel,
                onOpenCalculation = { id -> navController.navigate("calculation/$id") },
                onNewCalculation = { navController.navigate(AppDestination.NewCalculation.route) },
                onOpenMonthCalculations = { year, month ->
                    navController.navigate("month_calculations/$year/$month")
                }
            )
        }

        composable(AppDestination.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel
            )
        }

        composable(
            route = AppDestination.NewCalculation.ROUTE_PATTERN,
            arguments = listOf(
                navArgument("groupId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val initialGroupId = backStackEntry.arguments?.getString("groupId")
            val editorViewModel: CalculationEditorViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                key = "new_calculation_${initialGroupId ?: "root"}"
            ) {
                editorViewModelFactory()
            }
            CalculationEditorScreen(
                viewModel = editorViewModel,
                calculationId = null,
                initialGroupId = initialGroupId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.EditCalculation.ROUTE_PATTERN,
            arguments = listOf(navArgument("calculationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val calcId = backStackEntry.arguments?.getString("calculationId")
            val editorViewModel: CalculationEditorViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                key = "edit_calculation_$calcId"
            ) {
                editorViewModelFactory()
            }
            CalculationEditorScreen(
                viewModel = editorViewModel,
                calculationId = calcId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = AppDestination.MonthCalculations.ROUTE_PATTERN,
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: Calendar.getInstance().get(Calendar.YEAR)
            val month = backStackEntry.arguments?.getInt("month") ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
            MonthCalculationsScreen(
                year = year,
                month = month,
                repository = calculationRepository,
                onOpenCalculation = { id -> navController.navigate("calculation/$id") },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
