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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R

/**
 * Line spacing for the Actions Popup sheet.
 * Slightly larger than the homepage (38dp vs 29dp) for comfortable touch targets,
 * while maintaining authentic lined notebook paper rhythm.
 */
private val ActionSheetRuleSpacing: Dp = 38.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCalculationActionsSheet(
    calculationTitle: String,
    isPinned: Boolean = false,
    onTogglePin: () -> Unit = {},
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
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
                // Row 1: Calculation Title sitting directly on its ruled notebook line
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
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = calculationTitle.ifBlank { stringResource(R.string.editor_new_title) },
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = if (isRtl) 19.sp else 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = if (isRtl) 6.2.dp else 5.8.dp)
                    )
                }

                // Row 2: Épingler / Désépingler (Pin)
                ActionSheetRuledItem(
                    label = stringResource(if (isPinned) R.string.action_unpin else R.string.action_pin),
                    symbol = HisabiSymbol.Pin,
                    onClick = {
                        onDismiss()
                        onTogglePin()
                    }
                )

                // Row 3: Modifier (Edit)
                ActionSheetRuledItem(
                    label = stringResource(R.string.action_edit),
                    symbol = HisabiSymbol.Pencil,
                    onClick = {
                        onDismiss()
                        onEdit()
                    }
                )

                // Row 4: Dupliquer (Duplicate)
                ActionSheetRuledItem(
                    label = stringResource(R.string.action_duplicate),
                    symbol = HisabiSymbol.Copy,
                    onClick = {
                        onDismiss()
                        onDuplicate()
                    }
                )

                // Row 5: Supprimer (Delete - Destructive)
                ActionSheetRuledItem(
                    label = stringResource(R.string.action_delete),
                    symbol = HisabiSymbol.Trash,
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
 * Features a sketch icon on the start and handwritten label sitting directly on the line.
 */
@Composable
private fun ActionSheetRuledItem(
    label: String,
    symbol: HisabiSymbol,
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
        HisabiSketchIcon(
            symbol = symbol,
            contentDescription = null,
            tint = tintColor,
            size = 20.dp,
            modifier = Modifier.offset(y = if (isRtl) 1.5.dp else 1.0.dp)
        )

        Text(
            text = label,
            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
            fontSize = if (isRtl) 17.5.sp else 19.sp,
            fontWeight = if (isRtl) FontWeight.Medium else FontWeight.Normal,
            color = tintColor,
            style = TextStyle(platformStyle = NoFontPadding),
            modifier = Modifier.offset(y = if (isRtl) 6.0.dp else 5.5.dp)
        )
    }
}
