package com.cash.guide.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cash.guide.R
import com.cash.guide.data.SettingsRepository
import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.db.CalculationWithItems
import com.cash.guide.domain.DateGroupHelper
import com.cash.guide.domain.reminder.CreditReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    val repository: CalculationRepository,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var allItems: List<CalculationWithItems> = emptyList()
    private var lastContext: Context? = null

    init {
        if (settingsRepository != null) {
            viewModelScope.launch {
                settingsRepository.pinnedCalculationIds.collect { pinned ->
                    _uiState.update { it.copy(pinnedCalculationIds = pinned) }
                    lastContext?.let { applyFilters(it) }
                }
            }
            viewModelScope.launch {
                settingsRepository.userName.collect { name ->
                    _uiState.update { it.copy(userName = name) }
                }
            }
        }
    }

    fun togglePin(calculationId: String) {
        viewModelScope.launch {
            settingsRepository?.togglePinCalculation(calculationId)
        }
    }

    fun loadRecent(context: Context) {
        lastContext = context
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeAllSaved().collect { items ->
                allItems = items
                applyFilters(context)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        lastContext?.let { applyFilters(it) }
    }

    fun filterByDate(epochMs: Long?) {
        _uiState.update { it.copy(selectedDateEpoch = epochMs) }
        lastContext?.let { applyFilters(it) }
    }

    fun clearDateFilter() {
        filterByDate(null)
    }

    fun setPaymentFilter(filter: PaymentFilter) {
        _uiState.update { it.copy(selectedPaymentFilter = filter) }
        lastContext?.let { applyFilters(it) }
    }

    fun togglePaymentStatus(calculationId: String, currentStatus: String) {
        val nextStatus = if (currentStatus == "PAID") "UNPAID" else "PAID"
        viewModelScope.launch {
            repository.updatePaymentStatus(calculationId, nextStatus)
            if (nextStatus == "PAID") {
                lastContext?.let { CreditReminderScheduler.cancelReminder(it, calculationId) }
            }
        }
    }

    fun toggleCalcType(calculationId: String, currentType: String) {
        val nextType = if (currentType == "CREDIT") "PERSONNEL" else "CREDIT"
        val nextStatus = if (nextType == "CREDIT") "UNPAID" else "PAID"
        viewModelScope.launch {
            repository.updateCalcType(calculationId, nextType)
            repository.updatePaymentStatus(calculationId, nextStatus)
            if (nextType == "PERSONNEL") {
                lastContext?.let { CreditReminderScheduler.cancelReminder(it, calculationId) }
            }
        }
    }

    fun updateCreditDueDate(
        context: Context,
        calculation: CalculationWithItems,
        dueDateEpochMs: Long?,
        reminderEnabled: Boolean,
        reminderTimeEpochMs: Long?
    ) {
        viewModelScope.launch {
            repository.updateCreditDueDate(
                id = calculation.calculation.id,
                dueDateEpochMs = dueDateEpochMs,
                reminderEnabled = reminderEnabled,
                reminderTimeEpochMs = reminderTimeEpochMs
            )
            if (reminderEnabled && reminderTimeEpochMs != null && reminderTimeEpochMs > System.currentTimeMillis()) {
                val totalCentimes = calculation.totalCentimes
                val totalFormatted = String.format(java.util.Locale.US, "%.2f %s", totalCentimes / 100.0, calculation.calculation.currency)
                CreditReminderScheduler.scheduleReminder(
                    context = context,
                    calculationId = calculation.calculation.id,
                    title = calculation.calculation.title,
                    amountFormatted = totalFormatted,
                    reminderTimeEpochMs = reminderTimeEpochMs
                )
            } else {
                CreditReminderScheduler.cancelReminder(context, calculation.calculation.id)
            }
        }
    }

    private fun applyFilters(context: Context) {
        val query = _uiState.value.searchQuery.trim()
        val selectedDate = _uiState.value.selectedDateEpoch
        val paymentFilter = _uiState.value.selectedPaymentFilter
        val pinnedIds = _uiState.value.pinnedCalculationIds
        val favorites = allItems.filter { it.calculation.id in pinnedIds }

        val unpaidTotal = allItems
            .filter { (it.calculation.calcType == "CREDIT" || it.calculation.paymentStatus == "UNPAID") && it.calculation.paymentStatus == "UNPAID" }
            .sumOf { it.totalCentimes }

        val recentGroups = DateGroupHelper.groupByDate(
            items = allItems,
            todayString = context.getString(R.string.date_today),
            yesterdayString = context.getString(R.string.date_yesterday),
            thisWeekString = context.getString(R.string.date_this_week),
            locale = context.resources.configuration.locales[0]
        )

        if (query.isBlank() && selectedDate == null && paymentFilter == PaymentFilter.ALL) {
            _uiState.update {
                it.copy(
                    recentDateGroups = recentGroups,
                    filteredDateGroups = emptyList(),
                    favoriteCalculations = favorites,
                    unpaidTotalCentimes = unpaidTotal,
                    isLoading = false
                )
            }
        } else {
            val filtered = allItems.filter { calc ->
                val matchesQuery = if (query.isBlank()) true else {
                    calc.calculation.title.contains(query, ignoreCase = true) ||
                    calc.items.any { it.label.contains(query, ignoreCase = true) }
                }
                val matchesDate = if (selectedDate == null) true else {
                    isSameDay(calc.calculation.updatedAtEpochMs, selectedDate)
                }
                val matchesPayment = when (paymentFilter) {
                    PaymentFilter.ALL -> true
                    PaymentFilter.UNPAID -> calc.calculation.calcType == "CREDIT" || calc.calculation.paymentStatus == "UNPAID"
                    PaymentFilter.PAID -> calc.calculation.paymentStatus == "PAID"
                }
                matchesQuery && matchesDate && matchesPayment
            }

            val filteredGroups = DateGroupHelper.groupByDate(
                items = filtered,
                todayString = context.getString(R.string.date_today),
                yesterdayString = context.getString(R.string.date_yesterday),
                thisWeekString = context.getString(R.string.date_this_week),
                locale = context.resources.configuration.locales[0]
            )

            _uiState.update {
                it.copy(
                    recentDateGroups = recentGroups,
                    filteredDateGroups = filteredGroups,
                    favoriteCalculations = favorites,
                    unpaidTotalCentimes = unpaidTotal,
                    isLoading = false
                )
            }
        }
    }

    private fun isSameDay(epoch1: Long, epoch2: Long): Boolean {
        val c1 = java.util.Calendar.getInstance().apply { timeInMillis = epoch1 }
        val c2 = java.util.Calendar.getInstance().apply { timeInMillis = epoch2 }
        return c1.get(java.util.Calendar.YEAR) == c2.get(java.util.Calendar.YEAR) &&
               c1.get(java.util.Calendar.DAY_OF_YEAR) == c2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    fun selectCalculationForAction(calc: CalculationWithItems?) {
        _uiState.update { it.copy(selectedCalculationForAction = calc) }
    }

    fun requestDelete(calc: CalculationWithItems) {
        _uiState.update {
            it.copy(
                selectedCalculationForAction = null,
                calculationToDelete = calc
            )
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(calculationToDelete = null) }
    }

    fun confirmDelete() {
        val toDelete = _uiState.value.calculationToDelete ?: return
        viewModelScope.launch {
            repository.deleteCalculation(toDelete.calculation.id)
            lastContext?.let { CreditReminderScheduler.cancelReminder(it, toDelete.calculation.id) }
            _uiState.update { it.copy(calculationToDelete = null) }
        }
    }

    fun duplicateCalculation(calc: CalculationWithItems, onDuplicated: (String) -> Unit) {
        viewModelScope.launch {
            val duplicated = repository.duplicateCalculation(calc.calculation.id)
            if (duplicated != null) {
                onDuplicated(duplicated.calculation.id)
            }
        }
    }
}
