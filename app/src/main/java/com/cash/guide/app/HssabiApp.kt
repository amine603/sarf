package com.cash.guide.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.SettingsRepository
import com.cash.guide.data.TemplateRepository
import com.cash.guide.data.backup.BackupManager
import com.cash.guide.data.db.HssabiDatabase
import com.cash.guide.feature.editor.CalculationEditorViewModel
import com.cash.guide.feature.history.HistoryViewModel
import com.cash.guide.feature.home.HomeViewModel
import com.cash.guide.feature.groups.GroupsViewModel
import com.cash.guide.feature.settings.SettingsViewModel
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.NotebookBottomNavigation

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.util.Locale

class LocalizedContextWrapper(
    base: Context,
    val originalActivity: Activity?
) : ContextWrapper(base), ActivityResultRegistryOwner {
    override val activityResultRegistry: ActivityResultRegistry
        get() = (originalActivity as? ActivityResultRegistryOwner)?.activityResultRegistry
            ?: (baseContext as? ActivityResultRegistryOwner)?.activityResultRegistry
            ?: error("No ActivityResultRegistry available")
}

@Composable
fun HssabiApp() {
    val context = LocalContext.current
    val database = remember { HssabiDatabase.getInstance(context) }
    val calculationRepository = remember {
        CalculationRepository(database.calculationDao(), database.calculationGroupDao())
    }
    val settingsRepository = remember { SettingsRepository(context) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val calcDao = database.calculationDao()
            if (calcDao.getAllSaved().isEmpty()) {
                com.cash.guide.data.DataSeeder.seedCleanData(context)
            }
        }
    }

    val appLanguage by settingsRepository.appLanguage.collectAsState(initial = "fr")
    val configuration = LocalConfiguration.current

    val isArabicLanguage = appLanguage == "ar" || appLanguage == "dar" || appLanguage.startsWith("ar")
    val loc = remember(appLanguage) {
        when (appLanguage) {
            "dar" -> Locale("ar", "MA")
            "ar" -> Locale("ar")
            "en" -> Locale("en")
            else -> Locale("fr")
        }
    }
    val localizedConfig = remember(appLanguage, configuration) {
        Configuration(configuration).apply {
            setLocale(loc)
            setLayoutDirection(loc)
        }
    }
    androidx.compose.runtime.LaunchedEffect(loc, localizedConfig) {
        Locale.setDefault(loc)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(localizedConfig, context.resources.displayMetrics)
        (context as? Activity)?.let { act ->
            @Suppress("DEPRECATION")
            act.resources.updateConfiguration(localizedConfig, act.resources.displayMetrics)
        }
    }
    val localizedContext = remember(appLanguage, context) {
        LocalizedContextWrapper(
            context.createConfigurationContext(localizedConfig),
            context as? Activity
        )
    }
    val layoutDirection = if (isArabicLanguage) LayoutDirection.Rtl else LayoutDirection.Ltr

    val templateRepository = remember { TemplateRepository.getInstance(context) }
    val homeViewModel = viewModel { HomeViewModel(calculationRepository, settingsRepository) }
    val groupsViewModel = viewModel { GroupsViewModel(calculationRepository) }
    val historyViewModel = viewModel { HistoryViewModel(calculationRepository) }
    val backupManager = remember { BackupManager(database) }
    val settingsViewModel = viewModel { SettingsViewModel(settingsRepository, backupManager, calculationRepository) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isTopLevel = currentRoute in listOf(
        AppDestination.Home.route,
        AppDestination.Groups.route,
        AppDestination.History.route,
        AppDestination.Settings.route
    )

    val currentDestination = when (currentRoute) {
        AppDestination.Groups.route -> AppDestination.Groups
        AppDestination.History.route -> AppDestination.History
        AppDestination.Settings.route -> AppDestination.Settings
        else -> AppDestination.Home
    }

    val parentRegistryOwner = LocalActivityResultRegistryOwner.current
    val effectiveRegistryOwner = parentRegistryOwner ?: (context as? ActivityResultRegistryOwner) ?: localizedContext

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfig,
        LocalLayoutDirection provides layoutDirection,
        LocalActivityResultRegistryOwner provides effectiveRegistryOwner
    ) {
        Scaffold(
            bottomBar = {
                if (isTopLevel) {
                    NotebookBottomNavigation(
                        currentDestination = currentDestination,
                        onNavigateTo = { dest ->
                            if (currentRoute != dest.route) {
                                navController.navigate(dest.route) {
                                    popUpTo(AppDestination.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(JournalPaper)
                    .padding(
                        top = if (isTopLevel) innerPadding.calculateTopPadding() else 0.dp,
                        bottom = if (isTopLevel) innerPadding.calculateBottomPadding() else 0.dp
                    )
            ) {
                HssabiNavHost(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    groupsViewModel = groupsViewModel,
                    historyViewModel = historyViewModel,
                    settingsViewModel = settingsViewModel,
                    calculationRepository = calculationRepository,
                    settingsRepository = settingsRepository,
                    editorViewModelFactory = {
                        CalculationEditorViewModel(
                            calculationRepository = calculationRepository,
                            settingsRepository = settingsRepository,
                            templateRepository = templateRepository
                        )
                    }
                )
            }
        }
    }
}
