package com.cash.guide.feature.home

import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.CalculationDateGroup

enum class PaymentFilter {
    ALL,
    UNPAID,
    PAID
}

data class HomeUiState(
    val recentDateGroups: List<CalculationDateGroup> = emptyList(),
    val filteredDateGroups: List<CalculationDateGroup> = emptyList(),
    val favoriteCalculations: List<CalculationWithItems> = emptyList(),
    val pinnedCalculationIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedDateEpoch: Long? = null,
    val selectedPaymentFilter: PaymentFilter = PaymentFilter.ALL,
    val unpaidTotalCentimes: Long = 0L,
    val isLoading: Boolean = true,
    val selectedCalculationForAction: CalculationWithItems? = null,
    val calculationToDelete: CalculationWithItems? = null,
    val userName: String = "Youssef"
) {
    val isFiltering: Boolean
        get() = searchQuery.isNotBlank() || selectedDateEpoch != null || selectedPaymentFilter != PaymentFilter.ALL

    val displayDateGroups: List<CalculationDateGroup>
        get() = if (isFiltering) filteredDateGroups else recentDateGroups

    val isEmpty: Boolean
        get() = !isLoading && displayDateGroups.isEmpty()
}
