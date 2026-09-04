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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = JournalPaper,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
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
                        .width(36.dp)
                        .height(4.dp)
                        .background(JournalRule.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                )
            }
        }
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

            HorizontalDivider(color = JournalRule.copy(alpha = 0.5f), thickness = 0.6.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Action 1: Modifier
            ActionSheetItem(
                label = stringResource(R.string.action_edit),
                symbol = HisabiSymbol.More, // or pencil
                onClick = {
                    onDismiss()
                    onEdit()
                }
            )

            // Action 2: Dupliquer
            ActionSheetItem(
                label = stringResource(R.string.action_duplicate),
                symbol = HisabiSymbol.Page,
                onClick = {
                    onDismiss()
                    onDuplicate()
                }
            )

            // Action 3: Supprimer (Destructive)
            ActionSheetItem(
                label = stringResource(R.string.action_delete),
                symbol = HisabiSymbol.Trash,
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

@Composable
private fun ActionSheetItem(
    label: String,
    symbol: HisabiSymbol,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val tintColor = if (isDestructive) JournalActionDelete else JournalInk

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        HisabiSketchIcon(
            symbol = symbol,
            contentDescription = null,
            tint = tintColor,
            size = 20.dp
        )

        Text(
            text = label,
            fontFamily = ManropeFamily,
            fontSize = 16.sp,
            fontWeight = if (isDestructive) FontWeight.Bold else FontWeight.Medium,
            color = tintColor
        )
    }
}
