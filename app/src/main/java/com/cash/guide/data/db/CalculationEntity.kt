package com.cash.guide.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calculations",
    indices = [
        Index("updatedAtEpochMs"),
        Index("status"),
        Index("editingCalculationId"),
        Index("groupId"),
        Index("paymentStatus")
    ]
)
data class CalculationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val currency: String, // "DIRHAM" or "RIAL"
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val status: String, // "DRAFT" or "SAVED"
    val note: String? = null,
    val editingCalculationId: String? = null,
    val groupId: String? = null,
    val paymentStatus: String = "PAID" // "PAID" or "UNPAID"
)
