package com.cash.guide.feature.checklist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.domain.ChecklistShareHelper
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.JournalActionDelete
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.JournalTextKeyboardDock
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.journalBaselineOnRule
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    var showShareMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (state.activeInputTarget != ChecklistInputTarget.NONE) {
            viewModel.hideKeyboard()
        } else {
            onNavigateBack()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "checklist_cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                1f at 0
                1f at 499
                0f at 500
                0f at 999
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "cursor_blink"
    )

    val rowDotColors = remember {
        listOf(
            Color(0xFF3B82B6),
            Color(0xFFD65D82),
            Color(0xFF5E8C3B),
            Color(0xFFC7881E),
            Color(0xFF7E5AA8),
            Color(0xFFCC673B)
        )
    }

    val current = state.currentChecklist
    val items = current?.sortedItems ?: emptyList()
    val totalCount = current?.totalCount ?: 0
    val completedCount = current?.completedCount ?: 0
    val isAllCompleted = totalCount > 0 && completedCount == totalCount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JournalPaper)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = JournalPaper,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // --- Row 1: Back button + Title in Watercolor Pink Pill (Centered / Alone) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button, onClick = {
                                    if (state.activeInputTarget != ChecklistInputTarget.NONE) {
                                        viewModel.hideKeyboard()
                                    }
                                    onNavigateBack()
                                }),
                            contentAlignment = Alignment.Center
                        ) {
                            HisabiSketchIcon(
                                symbol = HisabiSymbol.Back,
                                contentDescription = stringResource(R.string.cd_back),
                                tint = JournalInk,
                                size = 20.dp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (state.activeInputTarget == ChecklistInputTarget.TITLE) HighlighterPink.copy(alpha = 0.55f)
                                    else HighlighterPink.copy(alpha = 0.35f)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    keyboardController?.hide()
                                    viewModel.focusTitle()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = (if (state.activeInputTarget == ChecklistInputTarget.TITLE) state.titleInput.text else current?.checklist?.title ?: "Checklist").ifBlank { "Checklist" },
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalWritingInk,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )

                                if (state.activeInputTarget == ChecklistInputTarget.TITLE && cursorAlpha > 0f) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(16.dp)
                                            .background(JournalWritingInk.copy(alpha = cursorAlpha))
                                    )
                                }
                            }
                        }

                        // Balancing Spacer so Title is centered
                        Spacer(modifier = Modifier.size(42.dp))
                    }

                    // --- Row 2: Sub-toolbar (Left: Count & Status, Right: Partager ↗) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$completedCount / $totalCount faits",
                                fontFamily = PatrickHandFamily,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAllCompleted) ColorEmerald else JournalWritingInk,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                            if (isAllCompleted) {
                                Text(
                                    text = "✓",
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorEmerald,
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HighlighterPink.copy(alpha = 0.45f))
                                .clickable(role = Role.Button) {
                                    viewModel.hideKeyboard()
                                    showShareMenu = true
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Partager ↗",
                                fontFamily = PatrickHandFamily,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )

                            DropdownMenu(
                                expanded = showShareMenu,
                                onDismissRequest = { showShareMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🖼️ Partager comme image (Image carnet)") },
                                    onClick = {
                                        showShareMenu = false
                                        val cl = state.currentChecklist ?: return@DropdownMenuItem
                                        ChecklistShareHelper.shareAsImage(
                                            context = context,
                                            checklistId = cl.checklist.id,
                                            title = cl.checklist.title,
                                            items = cl.sortedItems,
                                            isRtl = isRtl,
                                            coroutineScope = coroutineScope
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🔗 Partager sur WhatsApp (Texte + Lien cliquable)") },
                                    onClick = {
                                        showShareMenu = false
                                        val cl = state.currentChecklist ?: return@DropdownMenuItem
                                        ChecklistShareHelper.shareAsWhatsAppTextAndLink(
                                            context = context,
                                            title = cl.checklist.title,
                                            items = cl.sortedItems
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Divider line separating Header from notebook
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(JournalRule.copy(alpha = 0.35f))
                    )
                }
            }

            JournalRuledDocument(
                listState = listState,
                clearFocusOnTap = true,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // If there are completed items, show a clean, spacious "Supprimer les cochés" action at the top
                if (completedCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JournalRuleSpacing)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Supprimer les cochés ($completedCount)",
                            fontFamily = PatrickHandFamily,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorCoral,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier
                                .journalBaselineOnRule()
                                .clickable { viewModel.deleteCompletedItems() }
                        )
                    }
                }

                if (items.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JournalRuleSpacing)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Tapez un article ci-dessous pour commencer votre liste ✍️",
                            fontFamily = PatrickHandFamily,
                            fontSize = 15.sp,
                            color = JournalMutedInk.copy(alpha = 0.50f),
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.journalBaselineOnRule()
                        )
                    }
                }

                items.forEachIndexed { index, item ->
                    val rowNumber = index + 1
                    val dotColor = rowDotColors[index % rowDotColors.size]
                    var lineCount by remember(item.id, item.text) { mutableIntStateOf(1) }
                    var cachedLayout by remember(item.id, item.text) { mutableStateOf<TextLayoutResult?>(null) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = JournalRuleSpacing)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Left: Number (sitting on Rule 1)
                        Box(
                            modifier = Modifier
                                .height(JournalRuleSpacing)
                                .widthIn(min = 16.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(
                                text = "$rowNumber",
                                fontFamily = PatrickHandFamily,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = dotColor,
                                style = TextStyle(platformStyle = NoFontPadding),
                                modifier = Modifier.journalBaselineOnRule()
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Center: Item text (takes all available width, wraps up to 2 lines)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    viewModel.toggleItem(item.id, !item.isChecked)
                                }
                        ) {
                            Text(
                                text = item.text,
                                fontFamily = resolveJournalFont(item.text, isRtl),
                                fontSize = 16.sp,
                                lineHeight = 29.sp,
                                fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium,
                                color = if (item.isChecked) JournalMutedInk.copy(alpha = 0.55f) else JournalInk,
                                style = TextStyle(platformStyle = NoFontPadding, lineHeight = 29.sp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                onTextLayout = { layoutResult ->
                                    cachedLayout = layoutResult
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawWithContent {
                                        drawContent()
                                        if (item.isChecked) {
                                            val strikeColor = JournalWritingInk.copy(alpha = 0.35f)
                                            val layout = cachedLayout
                                            if (layout != null) {
                                                for (i in 0 until layout.lineCount) {
                                                    val lineTop = layout.getLineTop(i)
                                                    val lineBottom = layout.getLineBottom(i)
                                                    val strikeY = (lineTop + lineBottom) / 2f + 1f
                                                    val startX = layout.getLineLeft(i)
                                                    val endX = layout.getLineRight(i)
                                                    drawLine(
                                                        color = strikeColor,
                                                        start = Offset(startX, strikeY),
                                                        end = Offset(endX, strikeY),
                                                        strokeWidth = 1.2.dp.toPx(),
                                                        cap = StrokeCap.Round
                                                    )
                                                }
                                            } else {
                                                val strikeY = size.height * 0.52f
                                                drawLine(
                                                    color = strikeColor,
                                                    start = Offset(0f, strikeY),
                                                    end = Offset(size.width, strikeY),
                                                    strokeWidth = 1.2.dp.toPx(),
                                                    cap = StrokeCap.Round
                                                )
                                            }
                                        }
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right: [Checkbox] on the left, [Trash Icon] on the far right (Reversed!)
                        Row(
                            modifier = Modifier.height(JournalRuleSpacing),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Checkbox (left of trash)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(
                                        role = Role.Checkbox,
                                        onClickLabel = if (item.isChecked) "Décocher" else "Cocher",
                                        onClick = { viewModel.toggleItem(item.id, !item.isChecked) }
                                    )
                                    .journalBaselineOnRule(opticalOffsetFromBottom = 0.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Canvas(
                                    modifier = Modifier
                                        .size(19.dp)
                                        .offset(y = 1.dp)
                                ) {
                                    val strokeWidth = 1.35.dp.toPx()
                                    val corner = 3.dp.toPx()
                                    val rect = androidx.compose.ui.geometry.RoundRect(
                                        left = strokeWidth / 2f,
                                        top = strokeWidth / 2f,
                                        right = size.width - strokeWidth / 2f,
                                        bottom = size.height - strokeWidth / 2f,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
                                    )
                                    val path = Path().apply { addRoundRect(rect) }

                                    if (item.isChecked) {
                                        // Transparent background (no color fill)
                                        drawPath(
                                            path = path,
                                            color = Color(0xFF16A34A).copy(alpha = 0.85f),
                                            style = Stroke(width = strokeWidth)
                                        )
                                        val checkPath = Path().apply {
                                            moveTo(size.width * 0.20f, size.height * 0.52f)
                                            lineTo(size.width * 0.40f, size.height * 0.74f)
                                            lineTo(size.width * 0.82f, size.height * 0.22f)
                                        }
                                        drawPath(
                                            path = checkPath,
                                            color = Color(0xFF16A34A),
                                            style = Stroke(
                                                width = 2.dp.toPx(),
                                                cap = StrokeCap.Round,
                                                join = StrokeJoin.Round
                                            )
                                        )
                                    } else {
                                        drawPath(
                                            path = path,
                                            color = JournalInk.copy(alpha = 0.40f),
                                            style = Stroke(width = strokeWidth)
                                        )
                                    }
                                }
                            }

                            // Trash icon (far right)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable(
                                        role = Role.Button,
                                        onClickLabel = "Supprimer l'élément",
                                        onClick = { viewModel.deleteItem(item.id) }
                                    )
                                    .journalBaselineOnRule(opticalOffsetFromBottom = 0.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                HisabiSketchIcon(
                                    symbol = HisabiSymbol.Trash,
                                    contentDescription = "Supprimer",
                                    tint = JournalActionDelete.copy(alpha = 0.70f),
                                    size = 17.dp,
                                    modifier = Modifier.offset(y = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Delete list action at the bottom of the list
                if (items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(JournalRuleSpacing))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JournalRuleSpacing)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Supprimer cette liste",
                            fontFamily = PatrickHandFamily,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = ColorCoral.copy(alpha = 0.75f),
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier
                                .journalBaselineOnRule()
                                .clickable { showDeleteConfirmDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(JournalRuleSpacing * 3))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JournalPaper)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(JournalPaper)
                        .border(
                            BorderStroke(
                                width = 1.3.dp,
                                color = if (state.activeInputTarget == ChecklistInputTarget.ITEM_INPUT) JournalInk else JournalMutedInk.copy(alpha = 0.35f)
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            keyboardController?.hide()
                            viewModel.focusItemInput()
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "+",
                            fontFamily = PatrickHandFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk
                        )

                        if (state.inputText.text.isEmpty()) {
                            Text(
                                text = "Ajouter un élément (ex: Pain, Lait...)",
                                fontFamily = PatrickHandFamily,
                                fontSize = 15.sp,
                                color = JournalMutedInk.copy(alpha = 0.45f),
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                            if (state.activeInputTarget == ChecklistInputTarget.ITEM_INPUT && cursorAlpha > 0f) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(JournalInk.copy(alpha = cursorAlpha))
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = state.inputText.text,
                                    fontFamily = resolveJournalFont(state.inputText.text, isRtl),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = JournalInk,
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )
                                if (state.activeInputTarget == ChecklistInputTarget.ITEM_INPUT && cursorAlpha > 0f) {
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(16.dp)
                                            .background(JournalInk.copy(alpha = cursorAlpha))
                                    )
                                }
                            }
                        }
                    }

                    if (state.inputText.text.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HighlighterPink.copy(alpha = 0.65f))
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

            if (state.activeInputTarget != ChecklistInputTarget.NONE) {
                JournalTextKeyboardDock(
                    language = state.keyboardLanguage,
                    shiftMode = state.shiftMode,
                    expanded = state.keyboardExpanded,
                    onToggleExpand = { viewModel.toggleKeyboardExpanded() },
                    onCycleLanguage = { viewModel.cycleLanguage() },
                    onSelectLanguage = { viewModel.selectLanguage(it) },
                    onToggleShift = { viewModel.toggleShift() },
                    onInsertText = { viewModel.applyTextKey(it) },
                    onBackspace = { viewModel.applyTextBackspace() },
                    onSwitchToNumericMode = { },
                    onConfirm = { viewModel.confirmInput() }
                )
            } else {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = JournalPaper,
            title = {
                Text(
                    text = "Supprimer la checklist ?",
                    fontFamily = PatrickHandFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorCoral
                )
            },
            text = {
                Text(
                    text = "Êtes-vous sûr de vouloir supprimer cette checklist ? Cette action est irréversible.",
                    fontFamily = PatrickHandFamily,
                    fontSize = 16.sp,
                    color = JournalInk
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteCurrentChecklist()
                        onNavigateBack()
                    }
                ) {
                    Text(
                        text = "Supprimer",
                        fontFamily = PatrickHandFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorCoral
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler", fontFamily = PatrickHandFamily, fontSize = 16.sp)
                }
            }
        )
    }
}
