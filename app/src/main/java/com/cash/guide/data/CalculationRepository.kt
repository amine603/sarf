package com.cash.guide.data

import com.cash.guide.data.db.CalculationDao
import com.cash.guide.data.db.CalculationEntity
import com.cash.guide.data.db.CalculationItemEntity
import com.cash.guide.data.db.CalculationWithItems
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CalculationRepository(private val dao: CalculationDao) {

    fun observeRecentSaved(limit: Int = 10): Flow<List<CalculationWithItems>> =
        dao.observeRecentSaved(limit)

    fun observeAllSaved(): Flow<List<CalculationWithItems>> =
        dao.observeAllSaved()

    fun observeCalculation(id: String): Flow<CalculationWithItems?> =
        dao.observeCalculation(id)

    suspend fun getCalculation(id: String): CalculationWithItems? =
        dao.getCalculation(id)

    fun searchSaved(query: String): Flow<List<CalculationWithItems>> =
        dao.searchSaved(query.trim())

    suspend fun saveCalculation(
        calculation: CalculationEntity,
        items: List<CalculationItemEntity>
    ) {
        val now = System.currentTimeMillis()
        val targetId = calculation.editingCalculationId ?: calculation.id
        val existing = dao.getCalculation(targetId)
        val originalCreatedAt = existing?.calculation?.createdAtEpochMs
            ?: calculation.createdAtEpochMs.takeIf { it > 0 }
            ?: now

        val savedEntity = calculation.copy(
            id = targetId,
            status = "SAVED",
            createdAtEpochMs = originalCreatedAt,
            updatedAtEpochMs = now,
            editingCalculationId = null
        )
        val remappedItems = items.mapIndexed { index, item ->
            val existingItem = existing?.items?.firstOrNull { it.id == item.id }
            val itemCreatedAt = existingItem?.createdAtEpochMs
                ?: item.createdAtEpochMs.takeIf { it > 0 }
                ?: now
            item.copy(
                calculationId = targetId,
                position = index,
                createdAtEpochMs = itemCreatedAt,
                updatedAtEpochMs = now
            )
        }
        dao.upsertCalculationWithItems(savedEntity, remappedItems)
        // Clean up any drafts for this calculation
        if (calculation.id != targetId) {
            dao.deleteCalculation(calculation.id)
        }
        dao.deleteDrafts(targetId, targetId)
    }

    suspend fun saveDraft(
        calculation: CalculationEntity,
        items: List<CalculationItemEntity>
    ) {
        val draftEntity = calculation.copy(
            status = "DRAFT"
        )
        dao.upsertCalculationWithItems(draftEntity, items)
    }

    suspend fun deleteCalculation(id: String) {
        dao.deleteCalculation(id)
        dao.deleteDrafts(id, id)
    }

    suspend fun deleteDraft(draftId: String) {
        dao.deleteCalculation(draftId)
    }

    suspend fun getRecoverableDraft(editingCalculationId: String?): CalculationWithItems? {
        return if (editingCalculationId != null) {
            dao.getDraftForCalculation(editingCalculationId)
        } else {
            dao.getNewDraft()
        }
    }

    suspend fun duplicateCalculation(sourceId: String): CalculationWithItems? {
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        return dao.duplicateCalculation(sourceId, newId, now)
    }
}
