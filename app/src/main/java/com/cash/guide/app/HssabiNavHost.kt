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

import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HssabiNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    historyViewModel: HistoryViewModel,
    settingsViewModel: SettingsViewModel,
    editorViewModelFactory: () -> CalculationEditorViewModel,
    modifier: Modifier = Modifier
) {
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
                onOpenHistory = { navController.navigate(AppDestination.History.route) }
            )
        }

        composable(AppDestination.History.route) {
            HistoryScreen(
                viewModel = historyViewModel,
                onOpenCalculation = { id -> navController.navigate("calculation/$id") }
            )
        }

        composable(AppDestination.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel
            )
        }

        composable(AppDestination.NewCalculation.route) { backStackEntry ->
            val editorViewModel: CalculationEditorViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                key = "new_calculation"
            ) {
                editorViewModelFactory()
            }
            CalculationEditorScreen(
                viewModel = editorViewModel,
                calculationId = null,
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
    }
}
