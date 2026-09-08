package com.cash.guide.ui.notebook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R

/**
 * Line spacing for the Actions Popup sheet.
 * Slightly larger than the homepage (42dp vs 29dp) for comfortable touch targets
 * and badge placement, while maintaining authentic lined notebook paper rhythm.
 */
private val ActionSheetRuleSpacing: Dp = 42.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCalculationActionsSheet(
    calculationTitle: String,
    isPinned: Boolean = false,
    onTogglePin: () -> Unit = {},
    paymentStatus: String = "PAID",
    onTogglePaymentStatus: (() -> Unit)? = null,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onShareImage: (() -> Unit)? = null,
    onExportPdf: (() -> Unit)? = null,
    onExportExcel: (() -> Unit)? = null,
    onAssignToGroup: (() -> Unit)? = null,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = JournalPaper,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.5.dp)
                        .background(JournalRule.copy(alpha = 0.75f), RoundedCornerShape(2.dp))
                )
            }
        }
    ) {
        CompositionLocalProvider(
            LocalContext provides context,
            LocalConfiguration provides configuration,
            LocalLayoutDirection provides layoutDirection
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                // Row 1: Calculation Title sitting directly on its ruled notebook line (centered)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ActionSheetRuleSpacing)
                        .drawBehind {
                            val strokeW = 0.6.dp.toPx()
                            val y = size.height
                            drawLine(
                                color = JournalRule.copy(alpha = 0.55f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeW
                            )
                        }
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val titleText = calculationTitle.ifBlank { stringResource(R.string.editor_new_title) }
                    Text(
                        text = titleText,
                        fontFamily = resolveJournalFont(titleText, isRtl),
                        fontSize = if (isArabicScript(titleText) || isRtl) 16.sp else 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier
                            .fillMaxWidth()
                            .journalBaselineOnRule(lineHeight = ActionSheetRuleSpacing)
                    )
                }

                // Row 2: Épingler / Désépingler (Pin)
                ActionSheetRuledItem(
                    label = stringResource(if (isPinned) R.string.action_unpin else R.string.action_pin),
                    symbol = HisabiSymbol.Pin,
                    badgeColor = HighlighterBlue.copy(alpha = 0.50f),
                    onClick = {
                        onDismiss()
                        onTogglePin()
                    }
                )

                // Row: Marquer comme payé / Marquer comme crédit
                if (onTogglePaymentStatus != null) {
                    val isPaid = paymentStatus == "PAID"
                    val label = stringResource(if (isPaid) R.string.action_mark_unpaid else R.string.action_mark_paid)
                    val symbol = if (isPaid) HisabiSymbol.Clock else HisabiSymbol.Check
                    val badgeColor = if (isPaid) HighlighterYellow.copy(alpha = 0.55f) else HighlighterGreen.copy(alpha = 0.55f)
                    ActionSheetRuledItem(
                        label = label,
                        symbol = symbol,
                        badgeColor = badgeColor,
                        onClick = {
                            onDismiss()
                            onTogglePaymentStatus()
                        }
                    )
                }

                // Row 3: Modifier (Edit)
                ActionSheetRuledItem(
                    label = stringResource(R.string.action_edit),
                    symbol = HisabiSymbol.Pencil,
                    badgeColor = HighlighterPink.copy(alpha = 0.40f),
                    onClick = {
                        onDismiss()
                        onEdit()
                    }
                )

                // Row 4: Dupliquer (Duplicate)
                ActionSheetRuledItem(
                    label = stringResource(R.string.action_duplicate),
                    symbol = HisabiSymbol.Copy,
                    badgeColor = HighlighterYellow.copy(alpha = 0.55f),
                    onClick = {
                        onDismiss()
                        onDuplicate()
                    }
                )

                // Row: Partager en image (Share as Long Image)
                if (onShareImage != null) {
                    ActionSheetRuledItem(
                        label = stringResource(R.string.action_share_image),
                        symbol = HisabiSymbol.Share,
                        badgeColor = HighlighterGreen.copy(alpha = 0.55f),
                        onClick = {
                            onDismiss()
                            onShareImage()
                        }
                    )
                }

                // Row: Exporter en PDF
                if (onExportPdf != null) {
                    ActionSheetRuledItem(
                        label = stringResource(R.string.action_export_pdf),
                        symbol = HisabiSymbol.Page,
                        badgeColor = HighlighterBlue.copy(alpha = 0.50f),
                        onClick = {
                            onDismiss()
                            onExportPdf()
                        }
                    )
                }

                // Row: Exporter en Excel (.csv)
                if (onExportExcel != null) {
                    ActionSheetRuledItem(
                        label = stringResource(R.string.export_as_excel),
                        symbol = HisabiSymbol.Table,
                        badgeColor = HighlighterYellow.copy(alpha = 0.55f),
                        onClick = {
                            onDismiss()
                            onExportExcel()
                        }
                    )
                }

                // Row: Ajouter / Déplacer vers un groupe
                if (onAssignToGroup != null) {
                    ActionSheetRuledItem(
                        label = stringResource(R.string.action_add_to_group),
                        symbol = HisabiSymbol.Folder,
                        badgeColor = HighlighterBlue.copy(alpha = 0.55f),
                        onClick = {
                            onDismiss()
                            onAssignToGroup()
                        }
                    )
                }

                // Row 5: Supprimer (Delete - Destructive)
                ActionSheetRuledItem(
                    label = stringResource(R.string.action_delete),
                    symbol = HisabiSymbol.Trash,
                    badgeColor = JournalActionDelete.copy(alpha = 0.15f),
                    isDestructive = true,
                    onClick = {
                        onDismiss()
                        onDelete()
                    }
                )

                // Row 6: Empty notebook ruled line for authentic bottom margin
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ActionSheetRuleSpacing)
                        .drawBehind {
                            val strokeW = 0.6.dp.toPx()
                            val y = size.height
                            drawLine(
                                color = JournalRule.copy(alpha = 0.55f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeW
                            )
                        }
                )

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

/**
 * An action row inside the bottom sheet sitting directly on 1 notebook ruled line.
 * Features a colored sketch icon badge on the start and handwritten label sitting directly on the line.
 */
@Composable
private fun ActionSheetRuledItem(
    label: String,
    symbol: HisabiSymbol,
    badgeColor: Color,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val tintColor = if (isDestructive) JournalActionDelete else JournalInk

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ActionSheetRuleSpacing)
            .drawBehind {
                val strokeW = 0.6.dp.toPx()
                val y = size.height
                drawLine(
                    color = JournalRule.copy(alpha = 0.55f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeW
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(badgeColor)
                .offset(y = (-7).dp),
            contentAlignment = Alignment.Center
        ) {
            HisabiSketchIcon(
                symbol = symbol,
                contentDescription = null,
                tint = tintColor,
                size = 16.dp
            )
        }

        Text(
            text = label,
            fontFamily = resolveJournalFont(label, isRtl),
            fontSize = if (isRtl) 14.5.sp else 15.sp,
            fontWeight = FontWeight.Normal,
            color = tintColor,
            style = TextStyle(platformStyle = NoFontPadding),
            modifier = Modifier.journalBaselineOnRule(lineHeight = ActionSheetRuleSpacing)
        )
    }
}
