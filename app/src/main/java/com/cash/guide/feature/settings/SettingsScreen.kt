package com.cash.guide.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.HighlighterGreen
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.NotebookSectionBand
import com.cash.guide.ui.notebook.NotebookSegmentedControl
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.cash.guide.ui.notebook.JournalUpcomingFeatureRow

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    Box(modifier = modifier.fillMaxSize()) {
        JournalRuledDocument(modifier = Modifier.fillMaxSize()) {
            // Line 1: Header Band (Compact "Paramètres" / "الإعدادات" + Month Year sitting directly on ruled line 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 18.5.sp else 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                )

                val context = LocalContext.current
                val currentMonthYear = remember(context) {
                    val locale = context.resources.configuration.locales[0]
                    val sdf = java.text.SimpleDateFormat("LLLL yyyy", locale)
                    val raw = sdf.format(java.util.Date())
                    raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                }

                Text(
                    text = currentMonthYear,
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 14.5.sp else 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = JournalMutedInk.copy(alpha = 0.85f),
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                )
            }

            // Line 2: 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 3: Section 1 Header - Préférences (soft yellow band)
            NotebookSectionBand(
                title = stringResource(R.string.settings_section_preferences),
                highlightColor = HighlighterYellow,
                isCentered = false
            )

            // Lines 4-5: Setting 1 - Language (58dp = 2 rules)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing * 2)
            ) {
                // Line 1 (29dp): Label on Start, Segmented control on End
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(7.5.dp)
                                .offset(y = (-1.5).dp)
                        ) {
                            drawCircle(color = Color(0xFFE27B97)) // Soft Rose Pink
                        }

                        Text(
                            text = stringResource(R.string.settings_language),
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = if (isRtl) 16.5.sp else 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                        )
                    }

                    NotebookSegmentedControl(
                        options = listOf(
                            "fr" to stringResource(R.string.settings_french),
                            "ar" to stringResource(R.string.settings_arabic)
                        ),
                        selectedOption = if (state.currentLanguage.startsWith("ar")) "ar" else "fr",
                        onSelectOption = { viewModel.selectLanguage(it) }
                    )
                }

                // Line 2 (29dp): Description sitting on rule 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 29.5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.settings_language_description),
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = if (isRtl) 13.5.sp else 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                    )
                }
            }

            // Lines 6-7: Setting 2 - Currency (58dp = 2 rules)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing * 2)
            ) {
                // Line 1 (29dp): Label on Start, Segmented control on End
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(7.5.dp)
                                .offset(y = (-1.5).dp)
                        ) {
                            drawCircle(color = Color(0xFF5B9EC9)) // Soft Sky Blue
                        }

                        Text(
                            text = stringResource(R.string.settings_currency),
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = if (isRtl) 16.5.sp else 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                        )
                    }

                    NotebookSegmentedControl(
                        options = listOf(
                            MoneyUnit.DIRHAM to stringResource(R.string.currency_dirham),
                            MoneyUnit.RIAL to stringResource(R.string.currency_rial)
                        ),
                        selectedOption = state.defaultCurrency,
                        onSelectOption = { viewModel.selectDefaultCurrency(it) }
                    )
                }

                // Line 2 (29dp): Description sitting on rule 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 29.5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.settings_currency_description),
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = if (isRtl) 13.5.sp else 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                    )
                }
            }

            // Line 8: 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 9: Section 2 Header - Données (soft green band)
            NotebookSectionBand(
                title = stringResource(R.string.settings_section_data),
                highlightColor = HighlighterGreen,
                isCentered = false
            )

            // Lines 10-11: Setting 3 - Local Storage (58dp = 2 rules)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing * 2)
            ) {
                // Line 1 (29dp): Title on Start, Badge on End
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(7.5.dp)
                                .offset(y = (-1.5).dp)
                        ) {
                            drawCircle(color = Color(0xFF7FA85B)) // Soft Sage Green
                        }

                        Text(
                            text = stringResource(R.string.settings_storage_title),
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = if (isRtl) 16.5.sp else 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF7FA85B).copy(alpha = 0.20f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.settings_storage_badge),
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF436B2B),
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = if (isRtl) 1.0.dp else 0.5.dp)
                        )
                    }
                }

                // Line 2 (29dp): Storage Notice Text sitting on rule 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 29.5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.settings_storage_notice),
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = if (isRtl) 13.5.sp else 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
                    )
                }
            }

            // Line 12: 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 13: Section 3 Header - Upcoming Features (soft blue band)
            NotebookSectionBand(
                title = stringResource(R.string.settings_section_upcoming),
                highlightColor = HighlighterBlue,
                isCentered = false
            )

            // Lines 14-19: 3 Secondary Roadmap Rows (each 58dp = 2 rules)
            JournalUpcomingFeatureRow(
                title = stringResource(R.string.settings_backup_title),
                description = stringResource(R.string.settings_backup_desc)
            )

            JournalUpcomingFeatureRow(
                title = stringResource(R.string.settings_share_image_title),
                description = stringResource(R.string.settings_share_image_desc)
            )

            JournalUpcomingFeatureRow(
                title = stringResource(R.string.settings_export_title),
                description = stringResource(R.string.settings_export_desc)
            )

            // Bottom Spacers: 5 notebook lines for full scrolling clearance above dock
            Spacer(modifier = Modifier.height(JournalRuleSpacing * 5))
        }
    }
}
