package com.cash.guide.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calculation_items",
    foreignKeys = [
        ForeignKey(
            entity = CalculationEntity::class,
            parentColumns = ["id"],
            childColumns = ["calculationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("calculationId"),
        Index(value = ["calculationId", "position"], unique = true)
    ]
)
data class CalculationItemEntity(
    @PrimaryKey val id: String,
    val calculationId: String,
    val label: String,
    val amountCentimes: Long,
    val rawExpression: String? = null,
    val position: Int,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
