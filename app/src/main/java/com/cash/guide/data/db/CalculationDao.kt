package com.cash.guide.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface CalculationDao {

    @Transaction
    @Query("""
        SELECT * FROM calculations
        WHERE status = 'SAVED'
        ORDER BY updatedAtEpochMs DESC
        LIMIT :limit
    """)
    fun observeRecentSaved(limit: Int): Flow<List<CalculationWithItems>>

    @Transaction
    @Query("""
        SELECT * FROM calculations
        WHERE status = 'SAVED'
        ORDER BY updatedAtEpochMs DESC
    """)
    fun observeAllSaved(): Flow<List<CalculationWithItems>>

    @Transaction
    @Query("SELECT * FROM calculations WHERE id = :id")
    fun observeCalculation(id: String): Flow<CalculationWithItems?>

    @Transaction
    @Query("SELECT * FROM calculations WHERE id = :id")
    suspend fun getCalculation(id: String): CalculationWithItems?

    @Transaction
    @Query("""
        SELECT * FROM calculations 
        WHERE status = 'DRAFT' AND editingCalculationId = :editingCalculationId 
        ORDER BY updatedAtEpochMs DESC 
        LIMIT 1
    """)
    suspend fun getDraftForCalculation(editingCalculationId: String): CalculationWithItems?

    @Transaction
    @Query("""
        SELECT * FROM calculations 
        WHERE status = 'DRAFT' AND editingCalculationId IS NULL 
        ORDER BY updatedAtEpochMs DESC 
        LIMIT 1
    """)
    suspend fun getNewDraft(): CalculationWithItems?

    @Transaction
    @Query("""
        SELECT DISTINCT c.* FROM calculations c
        LEFT JOIN calculation_items i ON i.calculationId = c.id
        WHERE c.status = 'SAVED'
          AND (
            LOWER(c.title) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(COALESCE(c.note, '')) LIKE '%' || LOWER(:query) || '%'
            OR LOWER(COALESCE(i.label, '')) LIKE '%' || LOWER(:query) || '%'
          )
        ORDER BY c.updatedAtEpochMs DESC
    """)
    fun searchSaved(query: String): Flow<List<CalculationWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(calculation: CalculationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<CalculationItemEntity>)

    @Query("DELETE FROM calculation_items WHERE calculationId = :calculationId")
    suspend fun deleteItemsForCalculation(calculationId: String)

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteCalculation(id: String)

    @Query("DELETE FROM calculations WHERE status = 'DRAFT' AND (id = :draftId OR editingCalculationId = :targetId)")
    suspend fun deleteDrafts(draftId: String, targetId: String?)

    @Transaction
    suspend fun upsertCalculationWithItems(
        calculation: CalculationEntity,
        items: List<CalculationItemEntity>
    ) {
        insertCalculation(calculation)
        deleteItemsForCalculation(calculation.id)
        insertItems(items)
    }

    @Transaction
    suspend fun duplicateCalculation(
        sourceId: String,
        newId: String,
        now: Long
    ): CalculationWithItems? {
        val source = getCalculation(sourceId) ?: return null
        val duplicatedCalc = source.calculation.copy(
            id = newId,
            title = if (source.calculation.title.isNotBlank()) "${source.calculation.title} (copie)" else "",
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
            status = "DRAFT",
            editingCalculationId = null
        )
        val duplicatedItems = source.items.mapIndexed { index, item ->
            item.copy(
                id = UUID.randomUUID().toString(),
                calculationId = newId,
                position = index,
                createdAtEpochMs = now,
                updatedAtEpochMs = now
            )
        }
        upsertCalculationWithItems(duplicatedCalc, duplicatedItems)
        return getCalculation(newId)
    }

    @Transaction
    @Query("""
        SELECT * FROM calculations
        WHERE status = 'SAVED'
        ORDER BY updatedAtEpochMs DESC
    """)
    suspend fun getAllSaved(): List<CalculationWithItems>

    @Query("DELETE FROM calculations")
    suspend fun deleteAllCalculations()

    @Transaction
    suspend fun restoreCalculations(
        items: List<CalculationWithItems>,
        replaceExisting: Boolean
    ) {
        if (replaceExisting) {
            deleteAllCalculations()
        }
        for (item in items) {
            upsertCalculationWithItems(item.calculation, item.items)
        }
    }
}
