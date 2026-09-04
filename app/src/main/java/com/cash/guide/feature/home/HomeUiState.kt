package com.cash.guide.feature.home

import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.CalculationDateGroup

data class HomeUiState(
    val recentDateGroups: List<CalculationDateGroup> = emptyList(),
    val isLoading: Boolean = true,
    val selectedCalculationForAction: CalculationWithItems? = null,
    val calculationToDelete: CalculationWithItems? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && recentDateGroups.isEmpty()
}
