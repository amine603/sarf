package com.cash.guide.app

sealed class AppDestination(val route: String) {
    data object Home : AppDestination("home")
    data object History : AppDestination("history")
    data object Settings : AppDestination("settings")
    data object StyleShowcase : AppDestination("style_showcase")
    data object NewCalculation : AppDestination("calculation/new")
    data class EditCalculation(val calculationId: String) : AppDestination("calculation/$calculationId") {
        companion object {
            const val ROUTE_PATTERN = "calculation/{calculationId}"
        }
    }
    data class MonthCalculations(val year: Int, val month: Int) : AppDestination("month_calculations/$year/$month") {
        companion object {
            const val ROUTE_PATTERN = "month_calculations/{year}/{month}"
        }
    }
}
