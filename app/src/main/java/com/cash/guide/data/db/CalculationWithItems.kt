package com.cash.guide.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class CalculationWithItems(
    @Embedded val calculation: CalculationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "calculationId"
    )
    val items: List<CalculationItemEntity>
) {
    val totalCentimes: Long
        get() = items.sumOf { it.amountCentimes }
}
