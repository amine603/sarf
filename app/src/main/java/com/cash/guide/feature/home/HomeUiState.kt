package com.cash.guide.feature.home

import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.CalculationDateGroup

data class HomeUiState(
    val recentDateGroups: List<CalculationDateGroup> = emptyList(),
    val filteredDateGroups: List<CalculationDateGroup> = emptyList(),
    val searchQuery: String = "",
    val selectedDateEpoch: Long? = null,
    val isLoading: Boolean = true,
    val selectedCalculationForAction: CalculationWithItems? = null,
    val calculationToDelete: CalculationWithItems? = null
) {
    val isFiltering: Boolean
        get() = searchQuery.isNotBlank() || selectedDateEpoch != null

    val displayDateGroups: List<CalculationDateGroup>
        get() = if (isFiltering) filteredDateGroups else recentDateGroups

    val isEmpty: Boolean
        get() = !isLoading && displayDateGroups.isEmpty()
}
