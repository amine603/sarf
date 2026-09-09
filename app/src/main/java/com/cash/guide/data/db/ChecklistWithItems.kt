package com.cash.guide.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class ChecklistWithItems(
    @Embedded val checklist: ChecklistEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "checklistId"
    )
    val items: List<ChecklistItemEntity>
) {
    val sortedItems: List<ChecklistItemEntity>
        get() = items.sortedBy { it.position }

    val completedCount: Int
        get() = items.count { it.isChecked }

    val totalCount: Int
        get() = items.size

    val isAllCompleted: Boolean
        get() = items.isNotEmpty() && items.all { it.isChecked }
}
