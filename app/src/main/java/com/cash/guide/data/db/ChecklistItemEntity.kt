package com.cash.guide.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = ChecklistEntity::class,
            parentColumns = ["id"],
            childColumns = ["checklistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("checklistId"),
        Index(value = ["checklistId", "position"])
    ]
)
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    val checklistId: String,
    val text: String,
    val isChecked: Boolean = false,
    val position: Int,
    val createdAtEpochMs: Long
)
