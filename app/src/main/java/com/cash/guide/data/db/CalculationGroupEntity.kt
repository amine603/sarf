package com.cash.guide.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculation_groups")
data class CalculationGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
