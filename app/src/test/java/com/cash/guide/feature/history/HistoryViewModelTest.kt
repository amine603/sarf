package com.cash.guide.feature.history

import com.cash.guide.data.CalculationRepository
import com.cash.guide.data.db.CalculationDao
import com.cash.guide.data.db.CalculationEntity
import com.cash.guide.data.db.CalculationItemEntity
import com.cash.guide.data.db.CalculationWithItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeDao : CalculationDao {
        val calculations = mutableMapOf<String, CalculationEntity>()
        val items = mutableMapOf<String, MutableList<CalculationItemEntity>>()

        override fun observeRecentSaved(limit: Int): Flow<List<CalculationWithItems>> = flowOf(emptyList())
        override fun observeAllSaved(): Flow<List<CalculationWithItems>> {
            val list = calculations.values
                .filter { it.status == "SAVED" }
                .sortedByDescending { it.updatedAtEpochMs }
                .map { CalculationWithItems(it, items[it.id] ?: emptyList()) }
            return flowOf(list)
        }

        override fun observeCalculation(id: String): Flow<CalculationWithItems?> = flowOf(null)
        override suspend fun getCalculation(id: String): CalculationWithItems? {
            val c = calculations[id] ?: return null
            return CalculationWithItems(c, items[c.id] ?: emptyList())
        }
        override suspend fun getDraftForCalculation(editingCalculationId: String): CalculationWithItems? = null
        override suspend fun getNewDraft(): CalculationWithItems? = null

        override fun searchSaved(query: String): Flow<List<CalculationWithItems>> {
            val q = query.lowercase()
            val list = calculations.values
                .filter { it.status == "SAVED" }
                .filter { calc ->
                    calc.title.lowercase().contains(q) ||
                    (items[calc.id]?.any { it.label.lowercase().contains(q) } == true)
                }
                .sortedByDescending { it.updatedAtEpochMs }
                .map { CalculationWithItems(it, items[it.id] ?: emptyList()) }
            return flowOf(list)
        }

        override suspend fun insertCalculation(calculation: CalculationEntity) { calculations[calculation.id] = calculation }
        override suspend fun insertItems(items: List<CalculationItemEntity>) {
            items.forEach { this.items.getOrPut(it.calculationId) { mutableListOf() }.add(it) }
        }
        override suspend fun deleteItemsForCalculation(calculationId: String) { items.remove(calculationId) }
        override suspend fun deleteCalculation(id: String) { calculations.remove(id); items.remove(id) }
        override suspend fun deleteDrafts(draftId: String, targetId: String?) {}
        override suspend fun updatePaymentStatus(id: String, paymentStatus: String, now: Long) {
            val existing = calculations[id]
            if (existing != null) {
                calculations[id] = existing.copy(paymentStatus = paymentStatus, updatedAtEpochMs = now)
            }
        }
        override suspend fun updateCalcType(id: String, calcType: String, now: Long) {
            val existing = calculations[id]
            if (existing != null) {
                calculations[id] = existing.copy(calcType = calcType, updatedAtEpochMs = now)
            }
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

    private lateinit var dao: FakeDao
    private lateinit var repository: CalculationRepository
    private lateinit var viewModel: HistoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = FakeDao()
        repository = CalculationRepository(dao)
        viewModel = HistoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun updateSearchQuery_filtersCalculationsByTitleAndItemLabel() = runTest {
        val calc1 = CalculationEntity("1", "Factures Maison", "DIRHAM", 100, 100, "SAVED")
        dao.insertCalculation(calc1)
        dao.insertItems(listOf(CalculationItemEntity("i1", "1", "Internet", 25000, null, 0, 100, 100)))

        val calc2 = CalculationEntity("2", "Retrait Banque", "DIRHAM", 200, 200, "SAVED")
        dao.insertCalculation(calc2)
        dao.insertItems(listOf(CalculationItemEntity("i2", "2", "Maman", 60000, null, 0, 200, 200)))

        viewModel.updateSearchQuery("Maman")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSearching)
        assertEquals(1, state.searchResults.size)
        assertEquals("2", state.searchResults[0].calculation.id)

        // Clear query
        viewModel.updateSearchQuery("")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSearching)
        assertTrue(viewModel.uiState.value.searchResults.isEmpty())
    }
}
