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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.domain.NoteShareHelper
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
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
import com.cash.guide.ui.notebook.isArabicScript
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
    var menuExpanded by remember { mutableStateOf(false) }

    // Auto-save on back navigation
    BackHandler {
        if (uiState.activeInputTarget != NoteInputTarget.NONE) {
            viewModel.closeKeyboard()
        } else {
            viewModel.saveChanges()
            onNavigateBack()
        }
    }

    val dateFormatter = remember { SimpleDateFormat("d MMMM yyyy • HH:mm", Locale.getDefault()) }
    val formattedDate = remember(uiState.createdAtEpochMs) {
        val raw = dateFormatter.format(Date(uiState.createdAtEpochMs))
        raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    // Cursor blink animation (Pink highlighter cursor matching Image 3)
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

    val cursorColor = Color(0xFFE91E63) // Pinkish cursor line like Image 3

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
                    // Minimalist Top Bar: Back icon on left, Share & Menu (⋮) on right. NO TITLE!
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = JournalPaper,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Back Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable(role = Role.Button) {
                                        viewModel.closeKeyboard()
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

                            // Right Action Icons: Share + Overflow Menu (⋮)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Share button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable(role = Role.Button) {
                                            viewModel.closeKeyboard()
                                            viewModel.saveChanges()
                                            NoteShareHelper.shareAsImage(
                                                context = context,
                                                note = viewModel.getNoteEntity(),
                                                isRtl = isRtl,
                                                coroutineScope = coroutineScope
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    HisabiSketchIcon(
                                        symbol = HisabiSymbol.Share,
                                        contentDescription = "Partager",
                                        tint = JournalInk,
                                        size = 20.dp
                                    )
                                }

                                // Overflow Menu (⋮)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .clickable(role = Role.Button) { menuExpanded = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "⋮",
                                        fontFamily = PatrickHandFamily,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JournalInk
                                    )

                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false },
                                        modifier = Modifier.background(JournalPaper)
                                    ) {
                                        // Pin / Unpin
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (uiState.isPinned) {
                                                        if (isRtl) "إلغاء التثبيت 📌" else "Désépingler 📌"
                                                    } else {
                                                        if (isRtl) "تثبيت الملاحظة 📌" else "Épingler 📌"
                                                    },
                                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                    color = JournalWritingInk
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                viewModel.togglePin()
                                            }
                                        )

                                        // Color tag picker row
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    val colors = listOf(
                                                        "PINK" to Color(0xFFD85A8A),
                                                        "YELLOW" to Color(0xFFE5A823),
                                                        "BLUE" to Color(0xFF3B82F6),
                                                        "GREEN" to Color(0xFF5E9C47),
                                                        "PURPLE" to Color(0xFF8E44AD)
                                                    )
                                                    colors.forEach { (name, col) ->
                                                        Box(
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                                .clip(CircleShape)
                                                                .background(col)
                                                                .clickable {
                                                                    menuExpanded = false
                                                                    viewModel.setColorTag(name)
                                                                }
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = { }
                                        )

                                        // Delete Note
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (isRtl) "حذف الملاحظة 🗑" else "Supprimer 🗑",
                                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                    color = Color(0xFFE53935)
                                                )
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                showDeleteDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Ruled Document Content (Directly on Notebook Paper, NO Cards, NO Shadows)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        JournalRuledDocument(
                            modifier = Modifier.fillMaxSize(),
                            clearFocusOnTap = false
                        ) {
                            // Rule 1 (29dp): Title with soft highlighter pill background
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JournalRuleSpacing)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isTitleActive = uiState.activeInputTarget == NoteInputTarget.TITLE
                                val displayTitle = uiState.title.text

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(getNoteHighlightPillColor(uiState.colorTag))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            keyboardController?.hide()
                                            viewModel.focusTitle()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (displayTitle.isEmpty()) {
                                            Text(
                                                text = if (isRtl) "عنوان الملاحظة... ✍️" else "Titre de la note...",
                                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                fontSize = 17.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JournalWritingInk.copy(alpha = 0.45f),
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        } else {
                                            Text(
                                                text = displayTitle,
                                                fontFamily = resolveJournalFont(displayTitle, isRtl),
                                                fontSize = 17.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JournalWritingInk,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        }

                                        if (isTitleActive && cursorAlpha > 0f) {
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .height(18.dp)
                                                    .background(cursorColor)
                                            )
                                        }
                                    }
                                }
                            }

                            // Rule 2 (29dp): Metadata line (Date/Time on left, Pin + Color Dot on right)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JournalRuleSpacing)
                                    .padding(horizontal = 18.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Date & Time
                                Text(
                                    text = formattedDate,
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 13.sp,
                                    color = JournalMutedInk.copy(alpha = 0.75f),
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )

                                // Color dot + Pin
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Color dot (clickable to cycle colors)
                                    val colorsList = listOf("PINK", "YELLOW", "BLUE", "GREEN", "PURPLE")
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable(role = Role.Button) {
                                                val curIdx = colorsList.indexOf(uiState.colorTag.uppercase())
                                                val nextIdx = if (curIdx == -1) 1 else (curIdx + 1) % colorsList.size
                                                val nextTag = colorsList[nextIdx]
                                                viewModel.setColorTag(nextTag)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(getNoteDotColor(uiState.colorTag))
                                        )
                                    }

                                    // Pin icon (clickable to toggle pin)
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .clickable(role = Role.Button) {
                                                viewModel.togglePin()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "📌",
                                            fontSize = 15.sp,
                                            color = if (uiState.isPinned) Color.Unspecified else Color.Unspecified.copy(alpha = 0.35f)
                                        )
                                    }
                                }
                            }

                            // Rule 3 onwards: Multiline Content directly sitting on the blue lines
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp)
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

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    if (contentText.isEmpty()) {
                                    // Empty state: "Écrire une note..." sitting on Rule 3
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isContentActive && cursorAlpha > 0f) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.dp)
                                                            .height(18.dp)
                                                            .background(cursorColor)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                }
                                                Text(
                                                    text = if (isRtl) "اكتب ملاحظة..." else "Écrire une note...",
                                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                    fontSize = if (isRtl) 15.sp else 16.sp,
                                                    color = JournalMutedInk.copy(alpha = 0.45f),
                                                    style = TextStyle(platformStyle = NoFontPadding)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Multiline text broken into paragraphs and wrapped to exact 29dp rules
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
                                            // Empty paragraph takes exactly 1 rule (29dp)
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
                                                                .background(cursorColor)
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
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
                                                                    .background(cursorColor)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 4 blank rules at bottom for breathing room
                                Spacer(modifier = Modifier.height(JournalRuleSpacing * 4))
                                }
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
