package com.cash.guide.feature.note

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.domain.NoteShareHelper
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
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
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.resolveJournalFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Auto-save on back navigation
    BackHandler {
        if (uiState.activeInputTarget != NoteInputTarget.NONE) {
            viewModel.closeKeyboard()
        } else {
            viewModel.saveChanges()
            onNavigateBack()
        }
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE d MMMM yyyy • HH:mm", Locale.getDefault()) }
    val formattedDate = remember(uiState.createdAtEpochMs) {
        dateFormatter.format(Date(uiState.createdAtEpochMs)).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    // Cursor blink animation
    val infiniteTransition = rememberInfiniteTransition(label = "note_cursor")
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

    // Suppress system soft keyboard completely so in-app Journal keyboard is used
    CompositionLocalProvider(
        @Suppress("DEPRECATION")
        LocalTextInputService provides null
    ) {
        androidx.compose.ui.platform.InterceptPlatformTextInput(
            interceptor = { _, _ -> kotlinx.coroutines.awaitCancellation() }
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(JournalPaper)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Header Top Bar (matching ChecklistScreen style)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = JournalPaper,
                        tonalElevation = 0.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Row 1: Back button + Centered Title in Pink Pill + Balancing Spacer
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Back Button
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .clickable(role = Role.Button) {
                                            if (uiState.activeInputTarget != NoteInputTarget.NONE) {
                                                viewModel.closeKeyboard()
                                            }
                                            viewModel.saveChanges()
                                            onNavigateBack()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    HisabiSketchIcon(
                                        symbol = HisabiSymbol.Back,
                                        contentDescription = "Retour",
                                        tint = JournalInk,
                                        size = 20.dp
                                    )
                                }

                                // Centered Title in Watercolor Pink Pill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (uiState.activeInputTarget == NoteInputTarget.TITLE) HighlighterPink.copy(alpha = 0.55f)
                                            else HighlighterPink.copy(alpha = 0.35f)
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            keyboardController?.hide()
                                            viewModel.focusTitle()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        val displayTitle = if (uiState.title.text.isNotBlank()) {
                                            uiState.title.text
                                        } else {
                                            if (isRtl) "ملاحظة جديدة" else "Nouvelle note"
                                        }
                                        Text(
                                            text = displayTitle,
                                            fontFamily = resolveJournalFont(displayTitle, isRtl),
                                            fontSize = if (isRtl) 16.5.sp else 17.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalWritingInk,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )

                                        if (uiState.activeInputTarget == NoteInputTarget.TITLE && cursorAlpha > 0f) {
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

                            // Row 2: Sub-toolbar (Date on start, Share & Delete on end)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Date on start
                                Text(
                                    text = formattedDate,
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 13.sp,
                                    color = JournalMutedInk.copy(alpha = 0.75f),
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )

                                // Action buttons on end
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Partager ↗
                                    val shareText = if (isRtl) "مشاركة ↗" else "Partager ↗"
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable(role = Role.Button) {
                                                viewModel.closeKeyboard()
                                                viewModel.saveChanges()
                                                NoteShareHelper.shareAsImage(
                                                    context = context,
                                                    note = viewModel.getNoteEntity(),
                                                    isRtl = isRtl,
                                                    coroutineScope = coroutineScope
                                                )
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                            .drawBehind {
                                                val strokeW = 1.2.dp.toPx()
                                                val y = size.height + 1.dp.toPx()
                                                drawLine(
                                                    color = HighlighterPink,
                                                    start = Offset(0f, y),
                                                    end = Offset(size.width, y),
                                                    strokeWidth = strokeW,
                                                    cap = StrokeCap.Round
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = shareText,
                                            fontFamily = resolveJournalFont(shareText, isRtl),
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalInk,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }

                                    // Delete icon
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable(role = Role.Button) { showDeleteDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        HisabiSketchIcon(
                                            symbol = HisabiSymbol.Trash,
                                            contentDescription = "Supprimer",
                                            tint = JournalActionDelete.copy(alpha = 0.75f),
                                            size = 17.dp
                                        )
                                    }
                                }
                            }

                            // Divider
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(JournalRule.copy(alpha = 0.35f))
                            )
                        }
                    }

                    // Ruled Document Content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        JournalRuledDocument(
                            modifier = Modifier.fillMaxSize(),
                            clearFocusOnTap = false
                        ) {
                            // Rule 1 (29dp): Title input sitting on the first blue line
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JournalRuleSpacing)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        keyboardController?.hide()
                                        viewModel.focusTitle()
                                    }
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val isTitleActive = uiState.activeInputTarget == NoteInputTarget.TITLE
                                val displayTitle = uiState.title.text

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing)
                                        .journalBaselineOnRule(opticalOffsetFromBottom = 2.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    if (displayTitle.isEmpty()) {
                                        Text(
                                            text = if (isRtl) "عنوان الملاحظة... ✍️" else "Titre de la note... ✍️",
                                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalMutedInk.copy(alpha = 0.40f),
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    } else {
                                        Text(
                                            text = displayTitle,
                                            fontFamily = resolveJournalFont(displayTitle, isRtl),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalWritingInk,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }

                                    // Blinking cursor for Title
                                    if (isTitleActive && cursorAlpha > 0f) {
                                        val textMeasurer = rememberTextMeasurer()
                                        val titleStyle = TextStyle(
                                            fontFamily = resolveJournalFont(displayTitle, isRtl),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            platformStyle = NoFontPadding
                                        )
                                        val layout = textMeasurer.measure(
                                            text = AnnotatedString(displayTitle.take(uiState.title.selection.end)),
                                            style = titleStyle
                                        )
                                        val cursorX = if (isRtl) {
                                            -layout.size.width.toFloat()
                                        } else {
                                            layout.size.width.toFloat()
                                        }

                                        Box(
                                            modifier = Modifier
                                                .offset(x = with(LocalDensity.current) { cursorX.toDp() })
                                                .width(2.dp)
                                                .height(18.dp)
                                                .background(JournalWritingInk.copy(alpha = cursorAlpha))
                                        )
                                    }
                                }
                            }

                            // Rule 2 (29dp): Empty breathing spacer line
                            Spacer(modifier = Modifier.height(JournalRuleSpacing))

                            // Rule 3 onwards: Multiline note content rendered line-by-line directly on rules
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        keyboardController?.hide()
                                        viewModel.focusContent()
                                    }
                            ) {
                                val contentText = uiState.content.text
                                val isContentActive = uiState.activeInputTarget == NoteInputTarget.CONTENT
                                val textMeasurer = rememberTextMeasurer()
                                val availableWidthPx = constraints.maxWidth

                                val contentStyle = TextStyle(
                                    fontFamily = resolveJournalFont(contentText, isRtl),
                                    fontSize = if (isRtl) 16.sp else 16.5.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = JournalInk,
                                    platformStyle = NoFontPadding
                                )

                                if (contentText.isEmpty()) {
                                    // First content rule with placeholder
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(JournalRuleSpacing),
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .journalBaselineOnRule(opticalOffsetFromBottom = 2.dp),
                                            contentAlignment = Alignment.BottomStart
                                        ) {
                                            Text(
                                                text = if (isRtl) "اكتب أفكارك وملاحظاتك هنا..." else "Écrivez vos pensées et idées ici...",
                                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                fontSize = if (isRtl) 15.sp else 16.sp,
                                                color = JournalMutedInk.copy(alpha = 0.45f),
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                            if (isContentActive && cursorAlpha > 0f) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(2.dp)
                                                        .height(18.dp)
                                                        .background(JournalInk.copy(alpha = cursorAlpha))
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Split text into paragraphs
                                    val paragraphs = remember(contentText) { contentText.split('\n') }
                                    val paragraphStarts = remember(paragraphs) {
                                        val starts = mutableListOf<Int>()
                                        var cur = 0
                                        paragraphs.forEach { p ->
                                            starts.add(cur)
                                            cur += p.length + 1
                                        }
                                        starts
                                    }
                                    val cursorPosition = uiState.content.selection.end

                                    paragraphs.forEachIndexed { pIdx, paragraph ->
                                        val currentGlobalCharIndex = paragraphStarts[pIdx]

                                        if (paragraph.isEmpty()) {
                                            // Empty paragraph takes exactly 1 notebook rule
                                            val isCursorHere = isContentActive && (cursorPosition == currentGlobalCharIndex)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(JournalRuleSpacing)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        viewModel.focusContent(currentGlobalCharIndex)
                                                    },
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .journalBaselineOnRule(opticalOffsetFromBottom = 2.dp),
                                                    contentAlignment = Alignment.BottomStart
                                                ) {
                                                    if (isCursorHere && cursorAlpha > 0f) {
                                                        Box(
                                                            modifier = Modifier
                                                                .width(2.dp)
                                                                .height(18.dp)
                                                                .background(JournalInk.copy(alpha = cursorAlpha))
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            // Measure paragraph with text layout to wrap into lines
                                            val layoutResult = textMeasurer.measure(
                                                text = AnnotatedString(paragraph),
                                                style = contentStyle,
                                                constraints = Constraints(maxWidth = availableWidthPx)
                                            )
                                            val lineCount = layoutResult.lineCount

                                            for (lineIndex in 0 until lineCount) {
                                                val lineStartInP = layoutResult.getLineStart(lineIndex)
                                                val lineEndInP = layoutResult.getLineEnd(lineIndex)
                                                val lineText = paragraph.substring(lineStartInP, lineEndInP)

                                                val globalLineStart = currentGlobalCharIndex + lineStartInP
                                                val globalLineEnd = currentGlobalCharIndex + lineEndInP

                                                val isCursorInThisLine = isContentActive &&
                                                        (cursorPosition >= globalLineStart &&
                                                                (cursorPosition < globalLineEnd || (cursorPosition == globalLineEnd && lineIndex == lineCount - 1)))

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(JournalRuleSpacing)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        viewModel.focusContent(globalLineStart + lineText.length)
                                                    },
                                                    verticalAlignment = Alignment.Bottom
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .journalBaselineOnRule(opticalOffsetFromBottom = 2.dp),
                                                        contentAlignment = Alignment.BottomStart
                                                    ) {
                                                        Text(
                                                            text = lineText,
                                                            style = contentStyle
                                                        )

                                                        if (isCursorInThisLine && cursorAlpha > 0f) {
                                                            val offsetInLine = (cursorPosition - globalLineStart).coerceIn(0, lineText.length)
                                                            val subLayout = textMeasurer.measure(
                                                                text = AnnotatedString(lineText.take(offsetInLine)),
                                                                style = contentStyle
                                                            )
                                                            val cursorX = if (isRtl) {
                                                                -subLayout.size.width.toFloat()
                                                            } else {
                                                                subLayout.size.width.toFloat()
                                                            }

                                                            Box(
                                                                modifier = Modifier
                                                                    .offset(x = with(LocalDensity.current) { cursorX.toDp() })
                                                                    .width(2.dp)
                                                                    .height(18.dp)
                                                                    .background(JournalInk.copy(alpha = cursorAlpha))
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 3 rules of blank writing space at bottom
                                Spacer(modifier = Modifier.height(JournalRuleSpacing * 3))
                            }
                        }
                    }

                    // In-app Journal Keyboard Dock
                    if (uiState.activeInputTarget != NoteInputTarget.NONE) {
                        JournalTextKeyboardDock(
                            language = uiState.keyboardLanguage,
                            shiftMode = uiState.shiftMode,
                            expanded = uiState.keyboardExpanded,
                            onToggleExpand = { viewModel.toggleKeyboardExpanded() },
                            onCycleLanguage = { viewModel.cycleLanguage() },
                            onSelectLanguage = { viewModel.selectLanguage(it) },
                            onToggleShift = { viewModel.toggleShift() },
                            onInsertText = { viewModel.applyTextKey(it) },
                            onBackspace = { viewModel.applyTextBackspace() },
                            onSwitchToNumericMode = { },
                            onConfirm = { viewModel.confirmInput() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(JournalPaper)
                                .navigationBarsPadding()
                        )
                    }
                }

                // Delete Confirmation Dialog
                if (showDeleteDialog) {
                    val noteTitle = uiState.title.text.ifBlank { if (isRtl) "هذه الملاحظة" else "cette note" }
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = {
                            Text(
                                text = if (isRtl) "حذف الملاحظة ؟" else "Supprimer la note ?",
                                fontFamily = resolveJournalFont(noteTitle, isRtl),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk
                            )
                        },
                        text = {
                            Text(
                                text = if (isRtl) "واش متأكد باغي تمسح \"$noteTitle\" نهائياً؟" else "Êtes-vous sûr de vouloir supprimer définitivement \"$noteTitle\" ?",
                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                fontSize = 15.sp,
                                color = JournalInk
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                    viewModel.deleteNote {
                                        onNavigateBack()
                                    }
                                }
                            ) {
                                Text(
                                    text = if (isRtl) "حذف" else "Supprimer",
                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalActionDelete
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text(
                                    text = if (isRtl) "إلغاء" else "Annuler",
                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                    color = JournalMutedInk
                                )
                            }
                        },
                        containerColor = JournalPaper
                    )
                }
            }
        }
    }
}
