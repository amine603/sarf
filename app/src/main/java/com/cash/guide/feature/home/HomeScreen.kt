package com.cash.guide.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.ui.notebook.CalculationSummaryCard
import com.cash.guide.ui.notebook.DeleteConfirmationDialog
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.JournalDockBg
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.ManropeFamily
import com.cash.guide.ui.notebook.NotebookSearchField
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.SavedCalculationActionsSheet

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNewCalculation: () -> Unit,
    onOpenCalculation: (String) -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(context) {
        viewModel.loadRecent(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JournalPaper)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Identity
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = PatrickHandFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk
                    )
                    Text(
                        text = stringResource(R.string.app_tagline),
                        fontFamily = ManropeFamily,
                        fontSize = 14.sp,
                        color = JournalMutedInk
                    )
                }
            }

            // Search entry (tap navigates to History)
            item {
                NotebookSearchField(
                    query = "",
                    onQueryChange = {},
                    readOnly = true,
                    onClickWhenReadOnly = onOpenHistory
                )
            }

            // Primary CTA: Nouveau calcul
            item {
                Button(
                    onClick = onNewCalculation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HighlighterPink,
                        contentColor = JournalInk
                    ),
                    border = BorderStroke(1.dp, JournalRule.copy(alpha = 0.8f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Plus,
                            contentDescription = null,
                            tint = JournalInk,
                            size = 20.dp
                        )
                        Text(
                            text = stringResource(R.string.home_new_calculation),
                            fontFamily = PatrickHandFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Recent calculations header
            if (!state.isEmpty) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.home_recent_title),
                            fontFamily = PatrickHandFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk
                        )

                        Text(
                            text = stringResource(R.string.home_see_all),
                            fontFamily = ManropeFamily,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JournalMutedInk,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(role = Role.Button, onClick = onOpenHistory)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                // Date-grouped recent calculations
                state.recentDateGroups.forEach { group ->
                    item(key = "header_${group.header}") {
                        Text(
                            text = group.header,
                            fontFamily = ManropeFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalMutedInk,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    items(group.calculations, key = { it.calculation.id }) { calc ->
                        CalculationSummaryCard(
                            calculationWithItems = calc,
                            onClick = { onOpenCalculation(calc.calculation.id) },
                            onMoreClick = { viewModel.selectCalculationForAction(calc) }
                        )
                    }
                }
            } else if (!state.isLoading) {
                // Empty state
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = JournalDockBg,
                            border = BorderStroke(1.dp, JournalRule.copy(alpha = 0.6f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                HisabiSketchIcon(
                                    symbol = HisabiSymbol.Page,
                                    contentDescription = null,
                                    tint = JournalMutedInk,
                                    size = 28.dp
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.home_empty_title),
                            fontFamily = PatrickHandFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = stringResource(R.string.home_empty_body),
                            fontFamily = ManropeFamily,
                            fontSize = 14.sp,
                            color = JournalMutedInk,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Action Sheet
        val actionCalc = state.selectedCalculationForAction
        if (actionCalc != null) {
            SavedCalculationActionsSheet(
                calculationTitle = actionCalc.calculation.title,
                onEdit = {
                    onOpenCalculation(actionCalc.calculation.id)
                    viewModel.selectCalculationForAction(null)
                },
                onDuplicate = {
                    viewModel.duplicateCalculation(actionCalc) { newId ->
                        onOpenCalculation(newId)
                    }
                    viewModel.selectCalculationForAction(null)
                },
                onDelete = {
                    viewModel.requestDelete(actionCalc)
                },
                onDismiss = {
                    viewModel.selectCalculationForAction(null)
                }
            )
        }

        // Delete Confirmation Dialog
        if (state.calculationToDelete != null) {
            DeleteConfirmationDialog(
                onConfirmDelete = { viewModel.confirmDelete() },
                onDismiss = { viewModel.dismissDeleteDialog() }
            )
        }
    }
}
