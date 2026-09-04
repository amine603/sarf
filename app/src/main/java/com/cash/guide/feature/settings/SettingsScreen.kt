package com.cash.guide.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.JournalDockBg
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.ManropeFamily
import com.cash.guide.ui.notebook.PatrickHandFamily

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JournalPaper)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header: Title
            item {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontFamily = PatrickHandFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Setting Card 1: Language
            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_language),
                    description = stringResource(R.string.settings_language_description)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SelectableOptionPill(
                            label = stringResource(R.string.settings_french),
                            isSelected = state.currentLanguage.startsWith("fr"),
                            onClick = { viewModel.selectLanguage("fr") },
                            modifier = Modifier.weight(1f)
                        )

                        SelectableOptionPill(
                            label = stringResource(R.string.settings_arabic),
                            isSelected = state.currentLanguage.startsWith("ar"),
                            onClick = { viewModel.selectLanguage("ar") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Setting Card 2: Default Currency
            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_currency),
                    description = stringResource(R.string.settings_currency_description)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SelectableOptionPill(
                            label = stringResource(R.string.currency_dirham),
                            isSelected = state.defaultCurrency == MoneyUnit.DIRHAM,
                            onClick = { viewModel.selectDefaultCurrency(MoneyUnit.DIRHAM) },
                            modifier = Modifier.weight(1f)
                        )

                        SelectableOptionPill(
                            label = stringResource(R.string.currency_rial),
                            isSelected = state.defaultCurrency == MoneyUnit.RIAL,
                            onClick = { viewModel.selectDefaultCurrency(MoneyUnit.RIAL) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = JournalDockBg.copy(alpha = 0.65f),
        border = BorderStroke(0.75.dp, JournalRule.copy(alpha = 0.75f)),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontFamily = PatrickHandFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk
                )
                Text(
                    text = description,
                    fontFamily = ManropeFamily,
                    fontSize = 13.5.sp,
                    color = JournalMutedInk
                )
            }

            content()
        }
    }
}

@Composable
private fun SelectableOptionPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDesc = if (isSelected) stringResource(R.string.cd_selected) else stringResource(R.string.cd_not_selected)

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) HighlighterPink else Color.Transparent)
            .border(
                BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) JournalInk else JournalRule.copy(alpha = 0.8f)
                ),
                RoundedCornerShape(8.dp)
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp)
            .semantics {
                contentDescription = "$label, $selectedDesc"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = ManropeFamily,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = JournalInk
        )
    }
}
