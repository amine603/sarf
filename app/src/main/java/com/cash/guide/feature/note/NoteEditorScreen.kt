package com.cash.guide.feature.note

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.drawWithContent
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
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.JournalActionDelete
import com.cash.guide.ui.notebook.JournalHandFamily
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.JournalTextKeyboardDock
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.isArabicScript
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.resolveJournalFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NoteColorPalette(
    val id: String,
    val labelFr: String,
    val labelAr: String,
    val background: Color,
    val outline: Color,
    val pillColor: Color
)

fun getNoteCardColors(tag: String): NoteColorPalette {
    return when (tag.uppercase()) {
        "YELLOW" -> NoteColorPalette(
            id = "YELLOW",
            labelFr = "Jaune",
            labelAr = "أصفر",
            background = Color(0xFFFEFCE8),
            outline = Color(0xFFD97706),
            pillColor = HighlighterYellow
        )
        "PINK" -> NoteColorPalette(
            id = "PINK",
            labelFr = "Rose",
            labelAr = "وردي",
            background = Color(0xFFFDF2F8),
            outline = Color(0xFFE11D48),
            pillColor = HighlighterPink
        )
        "BLUE" -> NoteColorPalette(
            id = "BLUE",
            labelFr = "Bleu",
            labelAr = "أزرق",
            background = Color(0xFFEFF6FF),
            outline = Color(0xFF2563EB),
            pillColor = Color(0xFFBFDBFE)
        )
        "GREEN" -> NoteColorPalette(
            id = "GREEN",
            labelFr = "Vert",
            labelAr = "أخضر",
            background = Color(0xFFF0FDF4),
            outline = Color(0xFF059669),
            pillColor = Color(0xFFBBF7D0)
        )
        "PURPLE" -> NoteColorPalette(
            id = "PURPLE",
            labelFr = "Mauve",
            labelAr = "بنفسجي",
            background = Color(0xFFFAF5FF),
            outline = Color(0xFF7C3AED),
            pillColor = Color(0xFFE9D5FF)
        )
        else -> NoteColorPalette(
            id = "DEFAULT",
            labelFr = "Naturel",
            labelAr = "عادي",
            background = JournalPaper,
            outline = Color(0xFF3B82F6),
            pillColor = HighlighterPink.copy(alpha = 0.35f)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Intercept hardware or system back button to close custom keyboard first or save
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

    val availablePalettes = remember {
        listOf(
            getNoteCardColors("DEFAULT"),
            getNoteCardColors("YELLOW"),
            getNoteCardColors("PINK"),
            getNoteCardColors("BLUE"),
            getNoteCardColors("GREEN"),
            getNoteCardColors("PURPLE")
        )
    }
    val currentPalette = remember(uiState.colorTag) { getNoteCardColors(uiState.colorTag) }

    // Cursor animation for custom handwriting editor
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
                    // Header Top Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = JournalPaper,
                        tonalElevation = 0.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Back Button (Auto-save)
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .clickable(role = Role.Button) {
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

                                // Centered Title Pill reflecting chosen color
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(currentPalette.pillColor.copy(alpha = 0.50f))
                                            .border(
                                                BorderStroke(1.2.dp, currentPalette.outline.copy(alpha = 0.60f)),
                                                RoundedCornerShape(16.dp)
                                            )
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        val headerText = if (uiState.title.text.isNotBlank()) {
                                            uiState.title.text
                                        } else {
                                            if (isRtl) "ملاحظة جديدة" else "Nouvelle note"
                                        }
                                        Text(
                                            text = headerText,
                                            fontFamily = resolveJournalFont(headerText, isRtl),
                                            fontSize = if (isRtl) 15.sp else 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalWritingInk,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }
                                }

                                // Top Right Actions: Pin, Share, Delete
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Pin
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .clickable(role = Role.Button) { viewModel.togglePin() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (uiState.isPinned) "📌" else "📍",
                                            fontSize = 17.sp
                                        )
                                    }

                                    // Share Image
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .clickable(role = Role.Button) {
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
                                            size = 18.dp
                                        )
                                    }

                                    // Delete
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .clickable(role = Role.Button) { showDeleteDialog = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        HisabiSketchIcon(
                                            symbol = HisabiSymbol.Trash,
                                            contentDescription = "Supprimer",
                                            tint = JournalActionDelete.copy(alpha = 0.80f),
                                            size = 18.dp
                                        )
                                    }
                                }
                            }

                            // Color selection row with visible active highlights
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (isRtl) "اللون :" else "Couleur :",
                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalInk
                                )

                                availablePalettes.forEach { pal ->
                                    val isSelected = uiState.colorTag.equals(pal.id, ignoreCase = true)
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(pal.pillColor.copy(alpha = 0.70f))
                                            .border(
                                                BorderStroke(
                                                    if (isSelected) 2.2.dp else 1.dp,
                                                    if (isSelected) pal.outline else JournalRule.copy(alpha = 0.40f)
                                                ),
                                                CircleShape
                                            )
                                            .clickable(role = Role.Button) { viewModel.setColorTag(pal.id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Text(
                                                text = "✓",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = pal.outline
                                            )
                                        }
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
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    // Colored notebook vertical guide / margin line reflecting chosen color
                                    val marginX = if (isRtl) size.width - 24.dp.toPx() else 24.dp.toPx()
                                    drawLine(
                                        color = currentPalette.outline.copy(alpha = 0.50f),
                                        start = Offset(marginX, 0f),
                                        end = Offset(marginX, size.height),
                                        strokeWidth = 1.3.dp.toPx()
                                    )
                                },
                            clearFocusOnTap = false
                        ) {
                            // Rule 1 (29dp): Date stamp sitting on the first blue line
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JournalRuleSpacing)
                                    .padding(horizontal = 32.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = formattedDate,
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 13.sp,
                                    color = JournalMutedInk.copy(alpha = 0.80f),
                                    style = TextStyle(platformStyle = NoFontPadding),
                                    modifier = Modifier.journalBaselineOnRule(opticalOffsetFromBottom = 2.dp)
                                )
                            }

                            // Rule 2 (29dp): Title input sitting on the second blue line
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
                                    .padding(horizontal = 32.dp),
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
                                            text = if (isRtl) "عنوان الملاحظة..." else "Titre de la note...",
                                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                            fontSize = if (isRtl) 18.sp else 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalMutedInk.copy(alpha = 0.40f),
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    } else {
                                        Text(
                                            text = displayTitle,
                                            fontFamily = resolveJournalFont(displayTitle, isRtl),
                                            fontSize = if (isRtl) 18.sp else 19.sp,
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
                                            fontSize = if (isRtl) 18.sp else 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            platformStyle = NoFontPadding
                                        )
                                        val layout = textMeasurer.measure(
                                            text = AnnotatedString(displayTitle.take(uiState.title.selection.end)),
                                            style = titleStyle
                                        )
                                        val cursorX = if (isRtl) {
                                            // In RTL, cursor is to the left of the measured text
                                            -layout.size.width.toFloat()
                                        } else {
                                            layout.size.width.toFloat()
                                        }

                                        Box(
                                            modifier = Modifier
                                                .offset(x = with(LocalDensity.current) { cursorX.toDp() })
                                                .width(2.dp)
                                                .height(19.dp)
                                                .background(JournalWritingInk.copy(alpha = cursorAlpha))
                                        )
                                    }
                                }
                            }

                            // Rule 3 (29dp): Empty breathing space line
                            Spacer(modifier = Modifier.height(JournalRuleSpacing))

                            // Rule 4 onwards: Multiline note content rendered line-by-line directly on rules
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp)
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
                                                text = if (isRtl) "اكتب أفكارك وملاحظاتك هنا... ✍️" else "Écrivez vos pensées et idées ici... ✍️",
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
                                    // Measure and split text into lines matching width
                                    val layoutResult = remember(contentText, availableWidthPx, contentStyle) {
                                        textMeasurer.measure(
                                            text = AnnotatedString(contentText),
                                            style = contentStyle,
                                            constraints = Constraints(maxWidth = availableWidthPx)
                                        )
                                    }

                                    val lineCount = layoutResult.lineCount
                                    val cursorPos = uiState.content.selection.end.coerceIn(0, contentText.length)
                                    val activeLineIndex = layoutResult.getLineForOffset(cursorPos)

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        for (i in 0 until lineCount) {
                                            val start = layoutResult.getLineStart(i)
                                            val end = layoutResult.getLineEnd(i)
                                            val lineStr = contentText.substring(start, end).trimEnd('\r', '\n')

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(JournalRuleSpacing)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        keyboardController?.hide()
                                                        viewModel.focusContent(start + (lineStr.length / 2))
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
                                                        text = lineStr,
                                                        style = contentStyle
                                                    )

                                                    // Draw cursor on the active line
                                                    if (isContentActive && i == activeLineIndex && cursorAlpha > 0f) {
                                                        val lineCursorPos = cursorPos.coerceIn(start, end)
                                                        val cursorRect = layoutResult.getCursorRect(lineCursorPos)
                                                        Box(
                                                            modifier = Modifier
                                                                .offset(x = with(LocalDensity.current) { cursorRect.left.toDp() })
                                                                .width(2.dp)
                                                                .height(18.dp)
                                                                .background(JournalInk.copy(alpha = cursorAlpha))
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // If content ends with newline, show next line for cursor
                                        if (contentText.endsWith("\n") && isContentActive) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(JournalRuleSpacing),
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .journalBaselineOnRule(opticalOffsetFromBottom = 2.dp)
                                                ) {
                                                    if (cursorAlpha > 0f) {
                                                        Box(
                                                            modifier = Modifier
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

                            // 10 Empty Notebook Rules continuing to bottom
                            repeat(10) {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            keyboardController?.hide()
                                            viewModel.focusContent()
                                        }
                                )
                            }
                        }
                    }

                    // In-App Custom Journal Keyboard Dock
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
                            onConfirm = { viewModel.confirmInput() }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        val titleText = if (isRtl) "حذف الملاحظة ؟" else "Supprimer la note ?"
        val msgText = if (isRtl) "واش متأكد باغي تمسح هاد الملاحظة نهائياً؟" else "Êtes-vous sûr de vouloir supprimer cette note définitivement ?"
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = titleText,
                    fontFamily = resolveJournalFont(titleText, isRtl),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalWritingInk
                )
            },
            text = {
                Text(
                    text = msgText,
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
