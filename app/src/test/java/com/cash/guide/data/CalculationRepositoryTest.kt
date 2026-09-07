package com.cash.guide.data

import com.cash.guide.data.db.CalculationDao
import com.cash.guide.data.db.CalculationEntity
import com.cash.guide.data.db.CalculationItemEntity
import com.cash.guide.data.db.CalculationWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class CalculationRepositoryTest {

    private class FakeCalculationDao : CalculationDao {
        val calculations = mutableMapOf<String, CalculationEntity>()
        val items = mutableMapOf<String, MutableList<CalculationItemEntity>>()

        override fun observeRecentSaved(limit: Int): Flow<List<CalculationWithItems>> {
            val list = calculations.values
                .filter { it.status == "SAVED" }
                .sortedByDescending { it.updatedAtEpochMs }
                .take(limit)
                .map { CalculationWithItems(it, items[it.id] ?: emptyList()) }
            return flowOf(list)
        }

        override fun observeAllSaved(): Flow<List<CalculationWithItems>> {
            val list = calculations.values
                .filter { it.status == "SAVED" }
                .sortedByDescending { it.updatedAtEpochMs }
                .map { CalculationWithItems(it, items[it.id] ?: emptyList()) }
            return flowOf(list)
        }

        override fun observeCalculation(id: String): Flow<CalculationWithItems?> {
            val entity = calculations[id]
            val withItems = entity?.let { CalculationWithItems(it, items[it.id] ?: emptyList()) }
            return flowOf(withItems)
        }

        override suspend fun getCalculation(id: String): CalculationWithItems? {
            val entity = calculations[id] ?: return null
            return CalculationWithItems(entity, items[entity.id] ?: emptyList())
        }

        override suspend fun getDraftForCalculation(editingCalculationId: String): CalculationWithItems? {
            val entity = calculations.values
                .filter { it.status == "DRAFT" && it.editingCalculationId == editingCalculationId }
                .maxByOrNull { it.updatedAtEpochMs } ?: return null
            return CalculationWithItems(entity, items[entity.id] ?: emptyList())
        }

        override suspend fun getNewDraft(): CalculationWithItems? {
            val entity = calculations.values
                .filter { it.status == "DRAFT" && it.editingCalculationId == null }
                .maxByOrNull { it.updatedAtEpochMs } ?: return null
            return CalculationWithItems(entity, items[entity.id] ?: emptyList())
        }

        override fun searchSaved(query: String): Flow<List<CalculationWithItems>> {
            val q = query.lowercase()
            val list = calculations.values
                .filter { it.status == "SAVED" }
                .filter { calc ->
                    calc.title.lowercase().contains(q) ||
                    (calc.note?.lowercase()?.contains(q) == true) ||
                    (items[calc.id]?.any { it.label.lowercase().contains(q) } == true)
                }
                .sortedByDescending { it.updatedAtEpochMs }
                .map { CalculationWithItems(it, items[it.id] ?: emptyList()) }
            return flowOf(list)
        }

        override suspend fun insertCalculation(calculation: CalculationEntity) {
            calculations[calculation.id] = calculation
        }

        override suspend fun insertItems(items: List<CalculationItemEntity>) {
            items.forEach { item ->
                this.items.getOrPut(item.calculationId) { mutableListOf() }.add(item)
            }
        }

        override suspend fun deleteItemsForCalculation(calculationId: String) {
            items.remove(calculationId)
        }

        override suspend fun deleteCalculation(id: String) {
            calculations.remove(id)
            items.remove(id)
        }

        override suspend fun deleteDrafts(draftId: String, targetId: String?) {
            val toRemove = calculations.values
                .filter { it.status == "DRAFT" && (it.id == draftId || (targetId != null && it.editingCalculationId == targetId)) }
                .map { it.id }
            toRemove.forEach { deleteCalculation(it) }
        }

        override suspend fun getAllSaved(): List<CalculationWithItems> {
            return calculations.values
                .filter { it.status == "SAVED" }
                .sortedByDescending { it.updatedAtEpochMs }
                .map { CalculationWithItems(it, items[it.id] ?: emptyList()) }
        }

        override suspend fun deleteAllCalculations() {
            calculations.clear()
            items.clear()
        }
    }

    private lateinit var dao: FakeCalculationDao
    private lateinit var repository: CalculationRepository

    @Before
    fun setup() {
        dao = FakeCalculationDao()
        repository = CalculationRepository(dao)
    }

    @Test
    fun saveCalculation_commitsAtomicallyAndUpdatesTimestamps() = runBlocking {
        val calc = CalculationEntity(
            id = "c1",
            title = "Retrait banque",
            currency = "DIRHAM",
            createdAtEpochMs = 1000L,
            updatedAtEpochMs = 1000L,
            status = "DRAFT"
        )
        val items = listOf(
            CalculationItemEntity(
                id = "i1",
                calculationId = "c1",
                label = "Loyer",
                amountCentimes = 150_000L,
                position = 0,
                createdAtEpochMs = 1000L,
                updatedAtEpochMs = 1000L
            ),
            CalculationItemEntity(
                id = "i2",
                calculationId = "c1",
                label = "Maman",
                amountCentimes = 60_000L,
                position = 1,
                createdAtEpochMs = 1000L,
                updatedAtEpochMs = 1000L
            )
        )

        repository.saveCalculation(calc, items)

        val saved = repository.getCalculation("c1")
        assertNotNull(saved)
        assertEquals("SAVED", saved!!.calculation.status)
        assertEquals("Retrait banque", saved.calculation.title)
        assertEquals(2, saved.items.size)
        assertEquals(210_000L, saved.totalCentimes)
        assertTrue(saved.calculation.updatedAtEpochMs >= 1000L)
    }

    @Test
    fun backgroundDraft_neverMutatesSavedCalculationBeforeExplicitSave() = runBlocking {
        // Step 1: An existing calculation is saved
        val originalCalc = CalculationEntity(
            id = "saved1",
            title = "Original Title",
            currency = "DIRHAM",
            createdAtEpochMs = 1000L,
            updatedAtEpochMs = 1000L,
            status = "SAVED"
        )
        val originalItems = listOf(
            CalculationItemEntity(
                id = "orig_item1",
                calculationId = "saved1",
                label = "Original Item",
                amountCentimes = 50_000L,
                position = 0,
                createdAtEpochMs = 1000L,
                updatedAtEpochMs = 1000L
            )
        )
        repository.saveCalculation(originalCalc, originalItems)
        val originalSavedEpoch = repository.getCalculation("saved1")!!.calculation.updatedAtEpochMs

        // Step 2: User edits existing calculation -> background draft is saved
        val draftCalc = CalculationEntity(
            id = "draft_saved1",
            title = "Work in Progress Title",
            currency = "DIRHAM",
            createdAtEpochMs = 2000L,
            updatedAtEpochMs = 2000L,
            status = "DRAFT",
            editingCalculationId = "saved1"
        )
        val draftItems = listOf(
            CalculationItemEntity(
                id = "draft_item1",
                calculationId = "draft_saved1",
                label = "New Unconfirmed Row",
                amountCentimes = 999_000L,
                position = 0,
                createdAtEpochMs = 2000L,
                updatedAtEpochMs = 2000L
            )
        )
        repository.saveDraft(draftCalc, draftItems)

        // Step 3: Verify the saved calculation is COMPLETELY UNTOUCHED!
        val preserved = repository.getCalculation("saved1")
        assertNotNull(preserved)
        assertEquals("SAVED", preserved!!.calculation.status)
        assertEquals("Original Title", preserved.calculation.title)
        assertEquals(originalSavedEpoch, preserved.calculation.updatedAtEpochMs)
        assertEquals(1, preserved.items.size)
        assertEquals(50_000L, preserved.totalCentimes)

        // Step 4: Verify draft is recoverable
        val recoverable = repository.getRecoverableDraft("saved1")
        assertNotNull(recoverable)
        assertEquals("Work in Progress Title", recoverable!!.calculation.title)
        assertEquals(999_000L, recoverable.totalCentimes)

        // Step 5: Explicit Save commits draft onto saved1 and removes draft record
        repository.saveCalculation(draftCalc, draftItems)
        val updated = repository.getCalculation("saved1")
        assertNotNull(updated)
        assertEquals("Work in Progress Title", updated!!.calculation.title)
        assertEquals(999_000L, updated.totalCentimes)
        assertNull(repository.getRecoverableDraft("saved1"))
    }

    @Test
    fun saveCalculation_preservesOriginalCreatedAt_whenEditingExisting() = runBlocking {
        val initialTime = 1000L
        val originalCalc = CalculationEntity(
            id = "c_edit",
            title = "Initial Title",
            currency = "DIRHAM",
            createdAtEpochMs = initialTime,
            updatedAtEpochMs = initialTime,
            status = "SAVED"
        )
        val originalItems = listOf(
            CalculationItemEntity(
                id = "it1",
                calculationId = "c_edit",
                label = "First",
                amountCentimes = 10_000L,
                position = 0,
                createdAtEpochMs = initialTime,
                updatedAtEpochMs = initialTime
            )
        )
        repository.saveCalculation(originalCalc, originalItems)

        val laterTime = 5000L
        val editedCalc = CalculationEntity(
            id = "c_edit",
            title = "Updated Title",
            currency = "DIRHAM",
            createdAtEpochMs = laterTime,
            updatedAtEpochMs = laterTime,
            status = "SAVED"
        )
        val editedItems = listOf(
            CalculationItemEntity(
                id = "it1",
                calculationId = "c_edit",
                label = "First updated",
                amountCentimes = 12_000L,
                position = 0,
                createdAtEpochMs = laterTime,
                updatedAtEpochMs = laterTime
            )
        )
        repository.saveCalculation(editedCalc, editedItems)

        val updated = repository.getCalculation("c_edit")
        assertNotNull(updated)
        // Verify original createdAt is preserved forever!
        assertEquals(initialTime, updated!!.calculation.createdAtEpochMs)
        assertEquals("Updated Title", updated.calculation.title)
        assertTrue(updated.calculation.updatedAtEpochMs >= laterTime)
        assertEquals(initialTime, updated.items[0].createdAtEpochMs)
    }

    @Test
    fun duplicateCalculation_createsIndependentCopy_asDraftWithoutPollutingHistory() = runBlocking {
        val originalCalc = CalculationEntity(
            id = "source1",
            title = "Aïd Elkbir",
            currency = "DIRHAM",
            createdAtEpochMs = 1000L,
            updatedAtEpochMs = 1000L,
            status = "SAVED"
        )
        val originalItems = listOf(
            CalculationItemEntity(
                id = "item1",
                calculationId = "source1",
                label = "Hawli",
                amountCentimes = 300_000L,
                position = 0,
                createdAtEpochMs = 1000L,
                updatedAtEpochMs = 1000L
            )
        )
        repository.saveCalculation(originalCalc, originalItems)

        val duplicated = repository.duplicateCalculation("source1")
        assertNotNull(duplicated)
        assertNotEquals("source1", duplicated!!.calculation.id)
        assertEquals("Aïd Elkbir (copie)", duplicated.calculation.title)
        assertEquals("DRAFT", duplicated.calculation.status)
        assertEquals(1, duplicated.items.size)
        assertEquals("Hawli", duplicated.items[0].label)
        assertEquals(300_000L, duplicated.items[0].amountCentimes)
        assertNotEquals("item1", duplicated.items[0].id)
        assertEquals(duplicated.calculation.id, duplicated.items[0].calculationId)

        // Verify duplicate does NOT pollute saved queries
        var savedList: List<CalculationWithItems>? = null
        repository.observeAllSaved().collect { savedList = it }
        assertEquals(1, savedList?.size)
        assertEquals("source1", savedList?.first()?.calculation?.id)

        // Original is untouched
        val original = repository.getCalculation("source1")
        assertNotNull(original)
        assertEquals("source1", original!!.calculation.id)
    }

    @Test
    fun searchSaved_matchesTitleAndItemLabel() = runBlocking {
        val calc1 = CalculationEntity(id = "c1", title = "Factures", currency = "DIRHAM", createdAtEpochMs = 10, updatedAtEpochMs = 10, status = "SAVED")
        val items1 = listOf(CalculationItemEntity(id = "i1", calculationId = "c1", label = "Internet Orange", amountCentimes = 25_000L, position = 0, createdAtEpochMs = 10, updatedAtEpochMs = 10))

        val calc2 = CalculationEntity(id = "c2", title = "Retrait banque", currency = "DIRHAM", createdAtEpochMs = 20, updatedAtEpochMs = 20, status = "SAVED")
        val items2 = listOf(
            CalculationItemEntity(id = "i2", calculationId = "c2", label = "Maman", amountCentimes = 60_000L, position = 0, createdAtEpochMs = 20, updatedAtEpochMs = 20),
            CalculationItemEntity(id = "i3", calculationId = "c2", label = "Loyer", amountCentimes = 150_000L, position = 1, createdAtEpochMs = 20, updatedAtEpochMs = 20)
        )

        repository.saveCalculation(calc1, items1)
        repository.saveCalculation(calc2, items2)

        // Search by item label "Maman"
        var results: List<CalculationWithItems>? = null
        repository.searchSaved("Maman").collect { results = it }
        assertEquals(1, results?.size)
        assertEquals("c2", results?.first()?.calculation?.id)

        // Search by title "Factures"
        repository.searchSaved("factures").collect { results = it }
        assertEquals(1, results?.size)
        assertEquals("c1", results?.first()?.calculation?.id)
    }
}
