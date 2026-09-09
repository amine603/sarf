package com.cash.guide.feature.checklist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.data.db.ChecklistItemEntity
import com.cash.guide.domain.ChecklistShareHelper
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.resolveJournalFont

private val ColorEmerald = Color(0xFF1B7A4B)
private val ColorCoral = Color(0xFFD9534F)

@Composable
fun ChecklistScreen(
    viewModel: ChecklistViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val density = LocalDensity.current
    val rowHeightPx = with(density) { JournalRuleSpacing.toPx() }
    val ruleColor = JournalRule

    var showShareMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JournalPaper)
            .drawBehind {
                // Continuous background notebook blue ruled lines
                var y = rowHeightPx
                while (y < size.height) {
                    drawLine(
                        color = ruleColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 0.6.dp.toPx()
                    )
                    y += rowHeightPx
                }
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Line 1: Header bar (Back button, Title, Actions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back & Title
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(JournalRuleSpacing)
                            .clickable(
                                role = Role.Button,
                                onClick = onNavigateBack
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Back,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = JournalInk,
                            size = 20.dp,
                            modifier = Modifier.offset(y = (-4.5).dp)
                        )
                    }

                    Text(
                        text = "Checklist",
                        fontFamily = PatrickHandFamily,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                }

                // Top right action buttons: Switch/New Lists, Share, Menu
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Switch list button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HighlighterYellow.copy(alpha = 0.45f))
                            .clickable { viewModel.showListsDialog() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .offset(y = (-3).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "≡ Listes (${state.allChecklists.size})",
                                fontFamily = PatrickHandFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }
                    }

                    // Share button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HighlighterPink.copy(alpha = 0.45f))
                            .clickable { showShareMenu = true }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .offset(y = (-3).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Partager ↗",
                                fontFamily = PatrickHandFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }

                        DropdownMenu(
                            expanded = showShareMenu,
                            onDismissRequest = { showShareMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("🖼️ Partager comme image (Image carnet)") },
                                onClick = {
                                    showShareMenu = false
                                    val current = state.currentChecklist ?: return@DropdownMenuItem
                                    ChecklistShareHelper.shareAsImage(
                                        context = context,
                                        checklistId = current.checklist.id,
                                        title = current.checklist.title,
                                        items = current.sortedItems,
                                        isRtl = isRtl,
                                        coroutineScope = coroutineScope
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("💬 Partager sur WhatsApp (Texte + Lien cliquable)") },
                                onClick = {
                                    showShareMenu = false
                                    val current = state.currentChecklist ?: return@DropdownMenuItem
                                    ChecklistShareHelper.shareAsWhatsAppTextAndLink(
                                        context = context,
                                        title = current.checklist.title,
                                        items = current.sortedItems
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-header: Current Checklist Title & Counters (sitting on rule)
            val current = state.currentChecklist
            if (current != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Editable Title
                    if (state.isTitleEditing) {
                        BasicTextField(
                            value = state.titleInput,
                            onValueChange = { viewModel.setTitleInput(it) },
                            textStyle = TextStyle(
                                fontFamily = PatrickHandFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk,
                                platformStyle = NoFontPadding
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(JournalInk),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.saveTitle()
                                focusManager.clearFocus()
                            }),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "✓",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorEmerald,
                            modifier = Modifier
                                .clickable {
                                    viewModel.saveTitle()
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 6.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.startEditingTitle() },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = current.checklist.title,
                                fontFamily = PatrickHandFamily,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                            HisabiSketchIcon(
                                symbol = HisabiSymbol.Pencil,
                                contentDescription = null,
                                tint = JournalMutedInk.copy(alpha = 0.6f),
                                size = 13.dp
                            )
                        }
                    }

                    // Progress counter pill
                    val completed = current.completedCount
                    val total = current.totalCount
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (total > 0 && completed == total) ColorEmerald.copy(alpha = 0.15f)
                                else HighlighterBlue.copy(alpha = 0.35f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$completed / $total faits",
                            fontFamily = PatrickHandFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (total > 0 && completed == total) ColorEmerald else JournalWritingInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }
                }

                // Action sub-bar: (Tout cocher / Supprimer cochés)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (current.isAllCompleted) "Tout décocher" else "Tout cocher",
                            fontFamily = PatrickHandFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = JournalMutedInk,
                            modifier = Modifier
                                .clickable { viewModel.setAllItemsChecked(!current.isAllCompleted) }
                                .padding(vertical = 2.dp)
                        )
                        if (current.completedCount > 0) {
                            Text(
                                text = "• Supprimer les cochés",
                                fontFamily = PatrickHandFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorCoral,
                                modifier = Modifier
                                    .clickable { viewModel.deleteCompletedItems() }
                                    .padding(vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Supprimer la liste",
                        fontFamily = PatrickHandFamily,
                        fontSize = 12.sp,
                        color = ColorCoral.copy(alpha = 0.8f),
                        modifier = Modifier
                            .clickable { showDeleteConfirm = true }
                            .padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Items List
            val itemsList = current?.sortedItems ?: emptyList()
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (itemsList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(JournalRuleSpacing * 3),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tapez un article ci-dessous pour commencer votre liste ✍️",
                                fontFamily = PatrickHandFamily,
                                fontSize = 16.sp,
                                color = JournalMutedInk.copy(alpha = 0.6f),
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }
                    }
                } else {
                    items(itemsList, key = { it.id }) { item ->
                        ChecklistItemRow(
                            item = item,
                            onToggle = { isChecked -> viewModel.toggleItem(item.id, isChecked) },
                            onDelete = { viewModel.deleteItem(item.id) }
                        )
                    }
                }
            }

            // Bottom Docked Quick Input Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JournalPaper)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(
                            BorderStroke(1.2.dp, JournalWritingInk.copy(alpha = 0.5f)),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "+",
                        fontFamily = PatrickHandFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalWritingInk
                    )

                    BasicTextField(
                        value = state.inputText,
                        onValueChange = { viewModel.setInputText(it) },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            fontFamily = PatrickHandFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = JournalWritingInk,
                            platformStyle = NoFontPadding
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(JournalInk),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.addItem()
                        }),
                        decorationBox = { innerTextField ->
                            if (state.inputText.isEmpty()) {
                                Text(
                                    text = "Ajouter un élément (ex: Pain, Lait...)",
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 16.sp,
                                    color = JournalMutedInk.copy(alpha = 0.45f),
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (state.inputText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HighlighterPink.copy(alpha = 0.6f))
                                .clickable { viewModel.addItem() }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ajouter",
                                fontFamily = PatrickHandFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: List of Checklists / Create New
    if (state.showChecklistListDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissListsDialog() },
            title = {
                Text(
                    text = "Vos Checklists",
                    fontFamily = PatrickHandFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalWritingInk
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.allChecklists.forEach { cl ->
                        val isSelected = cl.checklist.id == state.currentChecklist?.checklist?.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) HighlighterYellow.copy(alpha = 0.4f)
                                    else Color.Transparent
                                )
                                .clickable { viewModel.selectChecklist(cl.checklist.id) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cl.checklist.title,
                                fontFamily = PatrickHandFamily,
                                fontSize = 17.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = JournalWritingInk
                            )
                            Text(
                                text = "${cl.completedCount}/${cl.totalCount}",
                                fontFamily = PatrickHandFamily,
                                fontSize = 14.sp,
                                color = JournalMutedInk
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = {
                            viewModel.dismissListsDialog()
                            viewModel.openCreateDialog()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "+ Créer une nouvelle liste",
                            fontFamily = PatrickHandFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorEmerald
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissListsDialog() }) {
                    Text("Fermer", fontFamily = PatrickHandFamily, fontSize = 15.sp)
                }
            }
        )
    }

    // Dialog: Create New Checklist
    if (state.showCreateNewDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCreateDialog() },
            title = {
                Text(
                    text = "Nouvelle Checklist",
                    fontFamily = PatrickHandFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                BasicTextField(
                    value = state.newChecklistTitle,
                    onValueChange = { viewModel.setNewChecklistTitle(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, JournalWritingInk), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    textStyle = TextStyle(
                        fontFamily = PatrickHandFamily,
                        fontSize = 17.sp,
                        color = JournalWritingInk
                    ),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (state.newChecklistTitle.isEmpty()) {
                            Text(
                                "Titre de la liste (ex: Courses, Souk...)",
                                fontFamily = PatrickHandFamily,
                                fontSize = 16.sp,
                                color = JournalMutedInk.copy(alpha = 0.5f)
                            )
                        }
                        inner()
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmCreateChecklist() }) {
                    Text("Créer", fontFamily = PatrickHandFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCreateDialog() }) {
                    Text("Annuler", fontFamily = PatrickHandFamily, fontSize = 15.sp)
                }
            }
        )
    }

    // Dialog: Confirm Delete
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer cette liste ?") },
            text = { Text("Êtes-vous sûr de vouloir supprimer définitivement cette checklist ?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteCurrentChecklist()
                }) {
                    Text("Supprimer", color = ColorCoral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun ChecklistItemRow(
    item: ChecklistItemEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(JournalRuleSpacing)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggle(!item.isChecked)
            }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Checkbox icon
            Canvas(modifier = Modifier.size(19.dp)) {
                val boxSize = size.width
                val corner = 4.dp.toPx()
                val strokeW = 1.6.dp.toPx()

                if (item.isChecked) {
                    drawRoundRect(
                        color = ColorEmerald.copy(alpha = 0.2f),
                        size = size,
                        cornerRadius = CornerRadius(corner, corner)
                    )
                    drawRoundRect(
                        color = ColorEmerald,
                        size = size,
                        cornerRadius = CornerRadius(corner, corner),
                        style = Stroke(width = strokeW)
                    )
                    // Checkmark
                    val p1 = Offset(boxSize * 0.22f, boxSize * 0.52f)
                    val p2 = Offset(boxSize * 0.44f, boxSize * 0.76f)
                    val p3 = Offset(boxSize * 0.80f, boxSize * 0.26f)
                    drawLine(ColorEmerald, p1, p2, strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(ColorEmerald, p2, p3, strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
                } else {
                    drawRoundRect(
                        color = JournalWritingInk.copy(alpha = 0.85f),
                        size = size,
                        cornerRadius = CornerRadius(corner, corner),
                        style = Stroke(width = strokeW)
                    )
                }
            }

            // Item Text
            Text(
                text = item.text,
                fontFamily = PatrickHandFamily,
                fontSize = 18.sp,
                fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                color = if (item.isChecked) JournalMutedInk.copy(alpha = 0.5f) else JournalWritingInk,
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(platformStyle = NoFontPadding)
            )
        }

        // Delete button '×'
        Text(
            text = "×",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = JournalMutedInk.copy(alpha = 0.4f),
            modifier = Modifier
                .clickable { onDelete() }
                .padding(horizontal = 8.dp)
        )
    }
}
