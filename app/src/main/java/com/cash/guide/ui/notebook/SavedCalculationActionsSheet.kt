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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = JournalPaper,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(JournalRule.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
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
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Calculation Title header
            Text(
                text = calculationTitle.ifBlank { stringResource(R.string.editor_new_title) },
                fontFamily = PatrickHandFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = JournalInk,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            HorizontalDivider(color = JournalRule.copy(alpha = 0.45f), thickness = 0.6.dp)

            // Action 1: Épingler / Désépingler (Pin)
            ActionSheetItem(
                label = stringResource(if (isPinned) R.string.action_unpin else R.string.action_pin),
                symbol = HisabiSymbol.Pin,
                badgeColor = HighlighterBlue.copy(alpha = 0.50f),
                onClick = {
                    onDismiss()
                    onTogglePin()
                }
            )

            HorizontalDivider(color = JournalRule.copy(alpha = 0.35f), thickness = 0.5.dp)

            // Action 2: Modifier
            ActionSheetItem(
                label = stringResource(R.string.action_edit),
                symbol = HisabiSymbol.Pencil,
                badgeColor = HighlighterPink.copy(alpha = 0.40f),
                onClick = {
                    onDismiss()
                    onEdit()
                }
            )

            HorizontalDivider(color = JournalRule.copy(alpha = 0.35f), thickness = 0.5.dp)

            // Action 2: Dupliquer
            ActionSheetItem(
                label = stringResource(R.string.action_duplicate),
                symbol = HisabiSymbol.Copy,
                badgeColor = HighlighterYellow.copy(alpha = 0.55f),
                onClick = {
                    onDismiss()
                    onDuplicate()
                }
            )

            HorizontalDivider(color = JournalRule.copy(alpha = 0.35f), thickness = 0.5.dp)

            // Action 3: Supprimer (Destructive)
            ActionSheetItem(
                label = stringResource(R.string.action_delete),
                symbol = HisabiSymbol.Trash,
                badgeColor = JournalActionDelete.copy(alpha = 0.15f),
                isDestructive = true,
                onClick = {
                    onDismiss()
                    onDelete()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
        }
    }
}

@Composable
private fun ActionSheetItem(
    label: String,
    symbol: HisabiSymbol,
    badgeColor: Color,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val tintColor = if (isDestructive) JournalActionDelete else JournalInk

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            HisabiSketchIcon(
                symbol = symbol,
                contentDescription = null,
                tint = tintColor,
                size = 18.dp
            )
        }

        Text(
            text = label,
            fontFamily = PatrickHandFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = tintColor
        )
    }
}
