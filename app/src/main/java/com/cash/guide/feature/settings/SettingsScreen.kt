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
import androidx.compose.foundation.border
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.cash.guide.ui.notebook.ExportOptionsBottomSheet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.NotebookMetrics

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.cash.guide.ui.notebook.JournalUpcomingFeatureRow
import com.cash.guide.ui.notebook.journalBaselineOnRule

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    var showExportOptions by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupToUri(context, uri) { success ->
                val msg = if (success) {
                    context.getString(R.string.backup_toast_export_success)
                } else {
                    context.getString(R.string.backup_toast_error)
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.loadBackupForInspection(context, uri) { success ->
                if (!success) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.backup_toast_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

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
                    fontSize = if (isRtl) 16.5.sp else 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule()
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
                    fontSize = if (isRtl) 13.5.sp else 14.sp,
                    fontWeight = FontWeight.Light,
                    color = JournalMutedInk.copy(alpha = 0.85f),
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule()
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
                // Line 1 (29dp): Label & bullet on Start, Description on End
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
                                .offset(y = (-2.5).dp)
                        ) {
                            drawCircle(color = Color(0xFFE27B97)) // Soft Rose Pink
                        }

                        Text(
                            text = stringResource(R.string.settings_language),
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = if (isRtl) 14.5.sp else 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.journalBaselineOnRule()
                        )
                    }

                    Text(
                        text = stringResource(R.string.settings_language_description),
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = if (isRtl) 11.5.sp else 12.sp,
                        fontWeight = FontWeight.Light,
                        color = JournalMutedInk.copy(alpha = 0.75f),
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.journalBaselineOnRule()
                    )
                }

                // Line 2 (29dp): 4 Language segmented control options sitting on rule 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    NotebookSegmentedControl(
                        options = listOf(
                            "fr" to stringResource(R.string.settings_french),
                            "en" to stringResource(R.string.settings_english),
                            "ar" to stringResource(R.string.settings_arabic),
                            "dar" to stringResource(R.string.settings_darija)
                        ),
                        selectedOption = when {
                            state.currentLanguage == "dar" -> "dar"
                            state.currentLanguage == "ar" -> "ar"
                            state.currentLanguage == "en" -> "en"
                            else -> "fr"
                        },
                        onSelectOption = { viewModel.selectLanguage(it) }
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
                                .offset(y = (-2.5).dp)
                        ) {
                            drawCircle(color = Color(0xFF5B9EC9)) // Soft Sky Blue
                        }

                        Text(
                            text = stringResource(R.string.settings_currency),
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = if (isRtl) 14.5.sp else 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.journalBaselineOnRule()
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
                        fontSize = if (isRtl) 11.5.sp else 12.sp,
                        fontWeight = FontWeight.Light,
                        color = JournalMutedInk.copy(alpha = 0.75f),
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.journalBaselineOnRule()
                    )
                }
            }

            // 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Section 2 Header - Données (soft green band)
            NotebookSectionBand(
                title = stringResource(R.string.settings_section_data),
                highlightColor = HighlighterGreen,
                isCentered = false
            )

            // Setting 3 - Local Storage (58dp = 2 rules)
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
                                .offset(y = (-2.5).dp)
                        ) {
                            drawCircle(color = Color(0xFF7FA85B)) // Soft Sage Green
                        }

                        Text(
                            text = stringResource(R.string.settings_storage_title),
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = if (isRtl) 14.5.sp else 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.journalBaselineOnRule()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(JournalRuleSpacing)
                            .drawBehind {
                                val h = size.height
                                val w = size.width
                                val washHeight = 20.dp.toPx()
                                val washCenterY = h - 6.dp.toPx()
                                val washY = washCenterY - (washHeight / 2f)
                                val padH = 8.dp.toPx()
                                val radius = CornerRadius(6.dp.toPx())
                                // Background tint
                                drawRoundRect(
                                    color = Color(0xFF7FA85B).copy(alpha = 0.20f),
                                    topLeft = Offset(-padH, washY),
                                    size = Size(w + padH * 2, washHeight),
                                    cornerRadius = radius
                                )
                                // Delicate outline
                                drawRoundRect(
                                    color = Color(0xFF7FA85B).copy(alpha = 0.45f),
                                    topLeft = Offset(-padH, washY),
                                    size = Size(w + padH * 2, washHeight),
                                    cornerRadius = radius,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = stringResource(R.string.settings_storage_badge),
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = if (isRtl) 11.5.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF436B2B),
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.journalBaselineOnRule()
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
                        fontSize = if (isRtl) 11.5.sp else 12.sp,
                        fontWeight = FontWeight.Light,
                        color = JournalMutedInk.copy(alpha = 0.75f),
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.journalBaselineOnRule()
                    )
                }
            }

            // Line 12: 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Section 3 Header - Sauvegarde & Restauration (soft blue band)
            NotebookSectionBand(
                title = stringResource(R.string.settings_section_backup),
                highlightColor = HighlighterBlue,
                isCentered = false
            )

            // Setting: Exporter une sauvegarde (.calc)
            JournalActionRow(
                title = stringResource(R.string.settings_backup_action_export),
                description = stringResource(R.string.settings_backup_action_export_desc),
                bulletColor = Color(0xFF5B9EC9),
                badgeText = ".calc",
                onClick = {
                    val dateSuffix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    exportLauncher.launch("sarf_backup_${dateSuffix}.calc")
                }
            )

            // Setting: Partager la sauvegarde (.calc)
            JournalActionRow(
                title = stringResource(R.string.settings_backup_action_share),
                description = stringResource(R.string.settings_backup_action_share_desc),
                bulletColor = Color(0xFF7FA85B),
                badgeText = stringResource(R.string.settings_action_badge_share),
                onClick = {
                    val chooserTitle = context.getString(R.string.backup_toast_share_title)
                    viewModel.shareBackup(context, chooserTitle) { success ->
                        if (!success) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.backup_toast_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )

            // Setting: Restaurer une sauvegarde (.calc)
            JournalActionRow(
                title = stringResource(R.string.settings_backup_action_restore),
                description = stringResource(R.string.settings_backup_action_restore_desc),
                bulletColor = Color(0xFFE27B97),
                badgeText = stringResource(R.string.settings_action_badge_restore),
                onClick = {
                    restoreLauncher.launch(arrayOf("*/*"))
                }
            )

            // 1 empty notebook line spacer
            Spacer(modifier = Modifier.height(JournalRuleSpacing))

            // Section 4 Header - Export & Reports (soft yellow band)
            NotebookSectionBand(
                title = stringResource(R.string.export_options_title),
                highlightColor = HighlighterYellow,
                isCentered = false
            )

            // Setting: Exporter PDF / Excel
            JournalActionRow(
                title = stringResource(R.string.settings_export_title),
                description = stringResource(R.string.settings_export_desc),
                bulletColor = Color(0xFFE5A93C),
                badgeText = stringResource(R.string.export_action_share),
                onClick = {
                    showExportOptions = true
                }
            )

            // Bottom Spacers: 5 notebook lines for full scrolling clearance above dock
            Spacer(modifier = Modifier.height(JournalRuleSpacing * 5))
        }

        // Restore confirmation dialog
        state.restoreCandidate?.let { payload ->
            RestoreBackupDialog(
                payload = payload,
                onMerge = {
                    viewModel.confirmRestore(replaceExisting = false) { success ->
                        val msg = if (success) {
                            context.getString(R.string.backup_toast_restore_success)
                        } else {
                            context.getString(R.string.backup_toast_error)
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onReplace = {
                    viewModel.confirmRestore(replaceExisting = true) { success ->
                        val msg = if (success) {
                            context.getString(R.string.backup_toast_restore_success)
                        } else {
                            context.getString(R.string.backup_toast_error)
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                onDismiss = {
                    viewModel.dismissRestoreDialog()
                }
            )
        }

        // Export Options Bottom Sheet
        if (showExportOptions) {
            ExportOptionsBottomSheet(
                title = stringResource(R.string.export_options_title),
                onExportPdf = {
                    viewModel.exportAllToPdf(context, isRtl) { success, hasCalcs ->
                        if (!hasCalcs) {
                            Toast.makeText(context, R.string.export_no_calculations, Toast.LENGTH_SHORT).show()
                        } else if (!success) {
                            Toast.makeText(context, R.string.export_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onExportExcel = {
                    viewModel.exportAllToExcel(context) { success, hasCalcs ->
                        if (!hasCalcs) {
                            Toast.makeText(context, R.string.export_no_calculations, Toast.LENGTH_SHORT).show()
                        } else if (!success) {
                            Toast.makeText(context, R.string.export_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDismiss = { showExportOptions = false }
            )
        }
    }
}

@Composable
private fun JournalActionRow(
    title: String,
    description: String,
    bulletColor: Color,
    badgeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(JournalRuleSpacing * 2)
            .clickable(onClick = onClick)
    ) {
        // Line 1 (29dp): Bullet + Title on Start, Badge on End
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
                        .offset(y = (-2.5).dp)
                ) {
                    drawCircle(color = bulletColor)
                }

                Text(
                    text = title,
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 14.5.sp else 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = JournalInk,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule()
                )
            }

            Box(
                modifier = Modifier
                    .height(JournalRuleSpacing)
                    .drawBehind {
                        val h = size.height
                        val w = size.width
                        val washHeight = 20.dp.toPx()
                        val washCenterY = h - 6.dp.toPx()
                        val washY = washCenterY - (washHeight / 2f)
                        val padH = 8.dp.toPx()
                        val radius = CornerRadius(6.dp.toPx())
                        // Soft tinted background
                        drawRoundRect(
                            color = bulletColor.copy(alpha = 0.16f),
                            topLeft = Offset(-padH, washY),
                            size = Size(w + padH * 2, washHeight),
                            cornerRadius = radius
                        )
                        // Delicate outline border
                        drawRoundRect(
                            color = bulletColor.copy(alpha = 0.50f),
                            topLeft = Offset(-padH, washY),
                            size = Size(w + padH * 2, washHeight),
                            cornerRadius = radius,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = badgeText,
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = if (isRtl) 11.5.sp else 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = bulletColor.copy(alpha = 0.95f),
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule()
                )
            }
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
                text = description,
                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                fontSize = if (isRtl) 11.5.sp else 12.sp,
                fontWeight = FontWeight.Light,
                color = JournalMutedInk.copy(alpha = 0.75f),
                style = TextStyle(platformStyle = NoFontPadding),
                modifier = Modifier.journalBaselineOnRule()
            )
        }
    }
}
