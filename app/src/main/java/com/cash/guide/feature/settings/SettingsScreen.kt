package com.cash.guide.feature.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.NoFontPadding
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
            // Line 1: Header Band (Single Screen Title in pink pill sitting directly on ruled line 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = 2.0.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HighlighterPink.copy(alpha = 0.40f))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }

            // Line 2: 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 3: Setting 1 - Language (sitting directly on ruled line 3)
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
                            .offset(y = 0.2.dp)
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
                        modifier = Modifier.offset(y = 5.7.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    JournalSelectableOption(
                        label = stringResource(R.string.settings_french),
                        isSelected = state.currentLanguage.startsWith("fr"),
                        highlightColor = HighlighterPink.copy(alpha = 0.45f),
                        onClick = { viewModel.selectLanguage("fr") }
                    )

                    JournalSelectableOption(
                        label = stringResource(R.string.settings_arabic),
                        isSelected = state.currentLanguage.startsWith("ar"),
                        highlightColor = HighlighterPink.copy(alpha = 0.45f),
                        isArabic = true,
                        onClick = { viewModel.selectLanguage("ar") }
                    )
                }
            }

            // Line 4: Language Description (sitting directly on ruled line 4)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = stringResource(R.string.settings_language_description),
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 13.5.sp else 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = JournalMutedInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.offset(y = 5.7.dp)
                )
            }

            // Line 5: 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 6: Setting 2 - Default Currency (sitting directly on ruled line 6)
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
                            .offset(y = 0.2.dp)
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
                        modifier = Modifier.offset(y = 5.7.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    JournalSelectableOption(
                        label = stringResource(R.string.currency_dirham),
                        isSelected = state.defaultCurrency == MoneyUnit.DIRHAM,
                        highlightColor = HighlighterYellow.copy(alpha = 0.65f),
                        isArabic = isRtl,
                        onClick = { viewModel.selectDefaultCurrency(MoneyUnit.DIRHAM) }
                    )

                    JournalSelectableOption(
                        label = stringResource(R.string.currency_rial),
                        isSelected = state.defaultCurrency == MoneyUnit.RIAL,
                        highlightColor = HighlighterYellow.copy(alpha = 0.65f),
                        isArabic = isRtl,
                        onClick = { viewModel.selectDefaultCurrency(MoneyUnit.RIAL) }
                    )
                }
            }

            // Line 7: Currency Description (sitting directly on ruled line 7)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = stringResource(R.string.settings_currency_description),
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 13.5.sp else 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = JournalMutedInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.offset(y = 5.7.dp)
                )
            }

            // Line 8: 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Line 9: Setting 3 - Storage Privacy Note (sitting directly on ruled line 9)
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
                            .offset(y = 0.2.dp)
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
                        modifier = Modifier.offset(y = 5.7.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .offset(y = 2.0.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF7FA85B).copy(alpha = 0.20f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_storage_badge),
                        fontFamily = PatrickHandFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF436B2B),
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }

            // Line 10: Storage Notice Text (sitting directly on ruled line 10)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = stringResource(R.string.settings_storage_notice),
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 13.5.sp else 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = JournalMutedInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.offset(y = 5.7.dp)
                )
            }

            // Lines 11 & 12: 2 empty notebook lines spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing * 2))

            // Line 13: Section Header - Upcoming Features (sitting directly on ruled line 13)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = 2.0.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(HighlighterPink.copy(alpha = 0.40f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_upcoming_title),
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding)
                    )
                }
            }

            // Line 14: 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Lines 15 & 16: Feature 1 - Local Backup
            JournalUpcomingFeatureRow(
                title = stringResource(R.string.settings_backup_title),
                description = stringResource(R.string.settings_backup_desc)
            )

            // Line 17: 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Lines 18 & 19: Feature 2 - Share as Image
            JournalUpcomingFeatureRow(
                title = stringResource(R.string.settings_share_image_title),
                description = stringResource(R.string.settings_share_image_desc)
            )

            // Line 20: 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Lines 21 & 22: Feature 3 - Export PDF / Excel
            JournalUpcomingFeatureRow(
                title = stringResource(R.string.settings_export_title),
                description = stringResource(R.string.settings_export_desc)
            )

            // Bottom Spacers: 5 notebook lines for full scrolling clearance above dock
            Spacer(modifier = Modifier.height(JournalRuleSpacing * 5))
        }
    }
}

@Composable
private fun JournalSelectableOption(
    label: String,
    isSelected: Boolean,
    highlightColor: Color,
    isArabic: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset(y = 2.0.dp)
            .clip(RoundedCornerShape(6.dp))
            .then(if (isSelected) Modifier.background(highlightColor) else Modifier)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = if (isArabic) TajawalFamily else PatrickHandFamily,
            fontSize = if (isArabic) 14.sp else 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) JournalInk else JournalMutedInk,
            style = TextStyle(platformStyle = NoFontPadding)
        )
    }
}
