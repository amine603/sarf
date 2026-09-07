package com.cash.guide.feature.home

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeDao : CalculationDao {
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

        override fun observeAllSaved(): Flow<List<CalculationWithItems>> = flowOf(emptyList())
        override fun observeCalculation(id: String): Flow<CalculationWithItems?> = flowOf(null)
        override suspend fun getCalculation(id: String): CalculationWithItems? {
            val c = calculations[id] ?: return null
            return CalculationWithItems(c, items[c.id] ?: emptyList())
        }
        override suspend fun getDraftForCalculation(editingCalculationId: String): CalculationWithItems? = null
        override suspend fun getNewDraft(): CalculationWithItems? = null
        override fun searchSaved(query: String): Flow<List<CalculationWithItems>> = flowOf(emptyList())
        override suspend fun insertCalculation(calculation: CalculationEntity) { calculations[calculation.id] = calculation }
        override suspend fun insertItems(items: List<CalculationItemEntity>) {
            items.forEach { this.items.getOrPut(it.calculationId) { mutableListOf() }.add(it) }
        }
        override suspend fun deleteItemsForCalculation(calculationId: String) { items.remove(calculationId) }
        override suspend fun deleteCalculation(id: String) { calculations.remove(id); items.remove(id) }
        override suspend fun deleteDrafts(draftId: String, targetId: String?) {}
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
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = FakeDao()
        repository = CalculationRepository(dao)
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun selectAndDismissDeleteDialog_managesStateCorrectly() = runTest {
        val calc = CalculationWithItems(
            CalculationEntity("1", "Test", "DIRHAM", 100, 100, "SAVED"),
            emptyList()
        )

        viewModel.selectCalculationForAction(calc)
        assertEquals("1", viewModel.uiState.value.selectedCalculationForAction?.calculation?.id)

        viewModel.requestDelete(calc)
        assertNull(viewModel.uiState.value.selectedCalculationForAction)
        assertEquals("1", viewModel.uiState.value.calculationToDelete?.calculation?.id)

        viewModel.dismissDeleteDialog()
        assertNull(viewModel.uiState.value.calculationToDelete)
    }

    @Test
    fun confirmDelete_deletesFromRepository() = runTest {
        val calcEntity = CalculationEntity("del-1", "Delete Me", "DIRHAM", 100, 100, "SAVED")
        dao.insertCalculation(calcEntity)
        val calc = CalculationWithItems(calcEntity, emptyList())

        viewModel.requestDelete(calc)
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.calculationToDelete)
        assertNull(dao.getCalculation("del-1"))
    }

    @Test
    fun duplicateCalculation_createsClonedEntry() = runTest {
        val calcEntity = CalculationEntity("src-1", "Source", "DIRHAM", 100, 100, "SAVED")
        dao.insertCalculation(calcEntity)
        val item = CalculationItemEntity("i-1", "src-1", "Item", 5000, null, 0, 100, 100)
        dao.insertItems(listOf(item))
        val calc = CalculationWithItems(calcEntity, listOf(item))

        var duplicatedId: String? = null
        viewModel.duplicateCalculation(calc) { id -> duplicatedId = id }
        advanceUntilIdle()

        assertNotNull(duplicatedId)
        val duplicated = repository.getCalculation(duplicatedId!!)
        assertNotNull(duplicated)
        assertEquals("Source (copie)", duplicated!!.calculation.title)
        assertEquals("DRAFT", duplicated.calculation.status)
        val allSaved = dao.calculations.values.filter { it.status == "SAVED" }
        assertEquals(1, allSaved.size)
        assertEquals("src-1", allSaved.first().id)
    }
}
