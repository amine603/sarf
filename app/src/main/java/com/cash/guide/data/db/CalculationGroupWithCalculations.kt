package com.cash.guide.data.db

data class CalculationGroupWithCalculations(
    val group: CalculationGroupEntity,
    val calculations: List<CalculationWithItems>
) {
    val totalCentimes: Long
        get() = calculations.sumOf { it.totalCentimes }

    val calculationCount: Int
        get() = calculations.size
}
