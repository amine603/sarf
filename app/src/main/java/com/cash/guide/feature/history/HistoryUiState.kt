package com.cash.guide.feature.history

import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.CalculationDateGroup

enum class HistoryFilter {
    ALL,
    THIS_MONTH
}

data class HistoryUiState(
    val searchQuery: String = "",
    val allDateGroups: List<CalculationDateGroup> = emptyList(),
    val searchResults: List<CalculationWithItems> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = true,
    val selectedFilter: HistoryFilter = HistoryFilter.ALL,
    val selectedCalculationForAction: CalculationWithItems? = null,
    val calculationToDelete: CalculationWithItems? = null
) {
    val isEmptyHistory: Boolean
        get() = !isLoading && allDateGroups.isEmpty() && searchQuery.isBlank()

    val isSearchEmpty: Boolean
        get() = !isLoading && isSearching && searchResults.isEmpty()
}
