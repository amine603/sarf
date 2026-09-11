package com.cash.guide.feature.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.data.db.NoteEntity
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.MonthPickerDialog
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.NotebookPrimaryActionButton
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.isArabicScript
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.resolveJournalFont
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class NoteColorOption(
    val tag: String,
    val lightColor: Color,
    val dotColor: Color
)

val NoteColorPalette = listOf(
    NoteColorOption("PINK", Color(0xFFFBCFE8), Color(0xFFE879A8)),       // Soft Rose
    NoteColorOption("PEACH", Color(0xFFFFD8B8), Color(0xFFF59E6B)),      // Soft Peach / Coral
    NoteColorOption("YELLOW", Color(0xFFFEF08A), Color(0xFFEAB308)),     // Soft Lemon Yellow
    NoteColorOption("AMBER", Color(0xFFFED7AA), Color(0xFFF59E0B)),      // Soft Warm Amber
    NoteColorOption("MINT", Color(0xFFBBF7D0), Color(0xFF34D399)),       // Soft Mint Green
    NoteColorOption("GREEN", Color(0xFFC8E6C9), Color(0xFF5E9C47)),      // Soft Sage Green
    NoteColorOption("TEAL", Color(0xFF99F6E4), Color(0xFF14B8A6)),       // Soft Aqua Teal
    NoteColorOption("SKY", Color(0xFFBAE6FD), Color(0xFF38BDF8)),        // Soft Sky Blue
    NoteColorOption("INDIGO", Color(0xFFC7D2FE), Color(0xFF818CF8)),     // Soft Periwinkle
    NoteColorOption("PURPLE", Color(0xFFE9D5FF), Color(0xFFA855F7)),     // Soft Lavender
    NoteColorOption("ROSE", Color(0xFFFCE7F3), Color(0xFFF43F5E)),       // Soft Blush Rose
    NoteColorOption("SAND", Color(0xFFF5E6D3), Color(0xFFD97706))        // Soft Warm Sand
)

/** Color helpers for notes accent dots & highlights */
fun getNoteDotColor(colorTag: String, fallbackIndex: Int = 0): Color {
    val upper = colorTag.uppercase()
    val match = NoteColorPalette.firstOrNull {
        it.tag == upper ||
        (it.tag == "SKY" && upper == "BLUE") ||
        (it.tag == "PEACH" && upper == "ORANGE")
    }
    return match?.dotColor ?: NoteColorPalette[fallbackIndex % NoteColorPalette.size].dotColor
}

fun getNoteHighlightPillColor(colorTag: String, fallbackIndex: Int = 0): Color {
    val upper = colorTag.uppercase()
    val match = NoteColorPalette.firstOrNull {
        it.tag == upper ||
        (it.tag == "SKY" && upper == "BLUE") ||
        (it.tag == "PEACH" && upper == "ORANGE")
    }
    return match?.lightColor ?: NoteColorPalette[fallbackIndex % NoteColorPalette.size].lightColor
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun NotesOverviewScreen(
    viewModel: NotesOverviewViewModel,
    onNavigateBack: () -> Unit,
    onOpenNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showMonthPicker by remember { mutableStateOf(false) }

    val shortDateFormatter = remember {
        SimpleDateFormat("d MMM", Locale.getDefault())
    }
    val currentMonthHeaderFormatter = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    }
    val currentMonthDisplay = remember {
        val raw = currentMonthHeaderFormatter.format(Date())
        raw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    // Search focus state
    var isSearchFocused by remember { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (!isImeVisible && isSearchFocused) {
            focusManager.clearFocus()
        }
    }

    if (isSearchFocused) {
        BackHandler {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    // Clear focus on open
    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JournalPaper)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar: Back button, Pink Highlight "Notes & idées" pill on left, Month/Year on right
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = JournalPaper,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Back button + "Notes & idées" soft pink pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Back Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button, onClick = onNavigateBack),
                            contentAlignment = Alignment.Center
                        ) {
                            HisabiSketchIcon(
                                symbol = HisabiSymbol.Back,
                                contentDescription = "Retour",
                                tint = JournalInk,
                                size = 20.dp
                            )
                        }

                        // Pink Highlighter Pill for Title: "Notes & idées"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HighlighterPink.copy(alpha = 0.45f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val titleText = if (isRtl) "ملاحظات وأفكار" else "Notes & idées"
                            Text(
                                text = titleText,
                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }
                    }

                    // Right: Current / Selected Month (e.g. "Septembre 2026")
                    val headerMonthText = if (uiState.selectedMonthKey != null) {
                        uiState.availableMonths.firstOrNull { it.first == uiState.selectedMonthKey }?.second
                            ?: uiState.selectedMonthKey!!
                    } else {
                        currentMonthDisplay
                    }

                    Text(
                        text = headerMonthText,
                        fontFamily = PatrickHandFamily,
                        fontSize = 15.sp,
                        color = JournalWritingInk.copy(alpha = 0.80f),
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            // Ruled Paper Content
            JournalRuledDocument(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                clearFocusOnTap = true
            ) {
                // Rule 1: Search Bar + Calendar Icon directly resting on the ruled blue line (29dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Search Capsule touching both blue lines
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(JournalRuleSpacing)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF6B7067).copy(alpha = 0.14f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                searchFocusRequester.requestFocus()
                                keyboardController?.show()
                            }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HisabiSketchIcon(
                                symbol = HisabiSymbol.Search,
                                contentDescription = null,
                                tint = JournalInk.copy(alpha = 0.65f),
                                size = 15.dp
                            )

                            val searchPlaceholder = if (isRtl) "بحث في الملاحظات..." else "Rechercher dans vos notes..."
                            BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(searchFocusRequester)
                                    .onFocusChanged { isSearchFocused = it.isFocused },
                                singleLine = true,
                                cursorBrush = if (isSearchFocused) SolidColor(JournalInk) else SolidColor(Color.Transparent),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                ),
                                textStyle = TextStyle(
                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                    fontSize = if (isRtl) 15.sp else 15.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = JournalInk,
                                    platformStyle = NoFontPadding
                                ),
                                decorationBox = { innerTextField ->
                                    if (uiState.searchQuery.isEmpty() && !isSearchFocused) {
                                        Text(
                                            text = searchPlaceholder,
                                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                            fontSize = if (isRtl) 13.5.sp else 14.5.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = JournalWritingInk.copy(alpha = 0.55f),
                                            style = TextStyle(platformStyle = NoFontPadding)
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            if (uiState.searchQuery.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable(role = Role.Button, onClick = {
                                            viewModel.updateSearchQuery("")
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                        }),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "✕",
                                        fontFamily = PatrickHandFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JournalMutedInk
                                    )
                                }
                            }
                        }
                    }

                    // Calendar icon button (29dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(JournalRuleSpacing)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.selectedMonthKey != null) HighlighterYellow.copy(alpha = 0.50f) else Color.Transparent)
                            .clickable(
                                role = Role.Button,
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    showMonthPicker = true
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Calendar,
                            contentDescription = stringResource(R.string.home_pick_date),
                            tint = if (uiState.selectedMonthKey != null) JournalInk else JournalInk.copy(alpha = 0.85f),
                            size = 23.dp
                        )
                    }
                }

                // Rule 2: 1-rule spacing (29dp)
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Rule 3: Section Badge "Notes récentes" with soft yellow highlight (29dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HighlighterYellow.copy(alpha = 0.45f))
                            .padding(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        val sectionText = if (isRtl) "أحدث الملاحظات" else "Notes récentes"
                        Text(
                            text = sectionText,
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalWritingInk,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }

                    if (uiState.selectedMonthKey != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        val activeMonthDisplay = uiState.availableMonths
                            .firstOrNull { it.first == uiState.selectedMonthKey }?.second ?: uiState.selectedMonthKey!!
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(HighlighterYellow.copy(alpha = 0.50f))
                                .clickable { viewModel.selectMonth(null) }
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$activeMonthDisplay ✕",
                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                // Rule 4: 1-rule breathing spacer before groups (29dp)
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Empty State
                if (uiState.monthGroups.isEmpty() && !uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JournalRuleSpacing * 6)
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Pencil,
                            contentDescription = null,
                            tint = JournalMutedInk.copy(alpha = 0.40f),
                            size = 38.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isRtl) "لا توجد أي ملاحظات حالياً" else "Aucune note trouvée",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalMutedInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRtl) "اضغط على الزر أسفله لكتابة أول ملاحظة ✍️" else "Appuyez sur le bouton ci-dessous pour créer une note ✍️",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 13.5.sp,
                            color = JournalMutedInk.copy(alpha = 0.70f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Month groups with vertical guide line connecting notes
                    val allNotes = remember(uiState.monthGroups) {
                        uiState.monthGroups.flatMap { it.notes }
                    }

                    uiState.monthGroups.forEach { monthGroup ->
                        // Month Tag Pill (e.g. "Septembre 2026")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(JournalRuleSpacing)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8EDD5))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = monthGroup.displayTitle,
                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalWritingInk,
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )
                            }
                        }

                        // Notes in this month group connected by a vertical guide
                        monthGroup.notes.forEachIndexed { noteIndex, note ->
                            val globalIndex = allNotes.indexOf(note).coerceAtLeast(0)
                            val dotColor = getNoteDotColor(note.colorTag, globalIndex)
                            val shortDateStr = shortDateFormatter.format(Date(note.updatedAtEpochMs))
                            val isLastInGroup = noteIndex == monthGroup.notes.lastIndex

                            NoteRowItem(
                                note = note,
                                dotColor = dotColor,
                                shortDateStr = shortDateStr,
                                isLastInGroup = isLastInGroup,
                                isRtl = isRtl,
                                onOpenNote = { onOpenNote(note.id) },
                                onTogglePin = { viewModel.togglePin(note.id, !note.isPinned) },
                                onSetColor = { colorTag -> viewModel.setColorTag(note.id, colorTag) },
                                onDelete = { viewModel.deleteNote(note.id) }
                            )
                        }

                        // 1-rule spacer after each group
                        Spacer(modifier = Modifier.height(JournalRuleSpacing))
                    }
                }

                // 1-rule spacer before action button
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Primary Action Button: "+ Nouvelle note" (29dp)
                NotebookPrimaryActionButton(
                    text = if (isRtl) "ملاحظة جديدة" else "Nouvelle note",
                    onClick = {
                        coroutineScope.launch {
                            val newId = viewModel.createNewNote()
                            onOpenNote(newId)
                        }
                    }
                )

                // Bottom extra space
                Spacer(modifier = Modifier.height(JournalRuleSpacing * 3))
            }
        }

        // Month Picker Dialog
        if (showMonthPicker) {
            val cal = remember { Calendar.getInstance() }
            val initialYear = remember(uiState.selectedMonthKey) {
                uiState.selectedMonthKey?.split("-")?.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
            }
            val initialMonth = remember(uiState.selectedMonthKey) {
                uiState.selectedMonthKey?.split("-")?.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)
            }
            MonthPickerDialog(
                initialYear = initialYear,
                initialMonth = initialMonth,
                onDismiss = { showMonthPicker = false },
                onSelectMonth = { year, month ->
                    showMonthPicker = false
                    val key = String.format(Locale.US, "%04d-%02d", year, month)
                    viewModel.selectMonth(key)
                }
            )
        }
    }
}

/**
 * Single Note Item rendered across exactly 2 notebook lines (58dp):
 * Line 1: Guide line with color dot + Title + 📌 (if pinned) + Date + Menu (⋮)
 * Line 2: Guide line continuing down + Content preview
 * Directly on ruled paper with zero card container!
 */
@Composable
private fun NoteRowItem(
    note: NoteEntity,
    dotColor: Color,
    shortDateStr: String,
    isLastInGroup: Boolean,
    isRtl: Boolean,
    onOpenNote: () -> Unit,
    onTogglePin: () -> Unit,
    onSetColor: (String) -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val displayTitle = note.title.ifBlank { if (isRtl) "ملاحظة بدون عنوان" else "Note sans titre" }
    val previewContent = note.content.lines().firstOrNull { it.isNotBlank() } ?: ""

    val guideLineColor = HighlighterPink.copy(alpha = 0.55f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(JournalRuleSpacing * 2) // exactly 58dp = 2 rule boxes!
            .clickable(role = Role.Button, onClick = onOpenNote)
    ) {
        // Line 1 (29dp): Guide line + dot + Title + Pin + Date + Menu (⋮)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(JournalRuleSpacing)
                .padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Guide line segment + Dot column
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(JournalRuleSpacing)
                    .drawBehind {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f

                        // Draw top half of vertical guide line
                        drawLine(
                            color = guideLineColor,
                            start = Offset(centerX, 0f),
                            end = Offset(centerX, centerY),
                            strokeWidth = 1.6.dp.toPx()
                        )

                        // If not last, draw bottom half of vertical guide line
                        if (!isLastInGroup) {
                            drawLine(
                                color = guideLineColor,
                                start = Offset(centerX, centerY),
                                end = Offset(centerX, size.height),
                                strokeWidth = 1.6.dp.toPx()
                            )
                        }

                        // Draw filled circle (dot)
                        drawCircle(
                            color = dotColor,
                            radius = 4.5.dp.toPx(),
                            center = Offset(centerX, centerY)
                        )
                    }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Title + Pin
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = displayTitle,
                    fontFamily = resolveJournalFont(displayTitle, isRtl),
                    fontSize = if (isArabicScript(displayTitle)) 16.sp else 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalWritingInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(platformStyle = NoFontPadding)
                )

                if (note.isPinned) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.PinFilled,
                        contentDescription = "Épinglé",
                        tint = JournalInk.copy(alpha = 0.85f),
                        size = 13.dp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Short Date (e.g. "10 sept.")
            Text(
                text = shortDateStr,
                fontFamily = PatrickHandFamily,
                fontSize = 13.sp,
                color = JournalMutedInk.copy(alpha = 0.70f),
                style = TextStyle(platformStyle = NoFontPadding)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // 3-dots Menu Button (⋮)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button) { menuExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⋮",
                    fontFamily = PatrickHandFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalMutedInk.copy(alpha = 0.85f)
                )

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(JournalPaper)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (note.isPinned) {
                                    if (isRtl) "إلغاء التثبيت" else "Désépingler"
                                } else {
                                    if (isRtl) "تثبيت الملاحظة" else "Épingler"
                                },
                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                color = JournalWritingInk
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onTogglePin()
                        }
                    )

                    // Color tag picker row (12 soft pastel colors in 2 rows)
                    DropdownMenuItem(
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val rows = NoteColorPalette.chunked(6)
                                rows.forEach { rowColors ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowColors.forEach { opt ->
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(opt.lightColor)
                                                    .border(0.8.dp, JournalInk.copy(alpha = 0.25f), CircleShape)
                                                    .clickable {
                                                        menuExpanded = false
                                                        onSetColor(opt.tag)
                                                    }
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        onClick = { }
                    )

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
                            onDelete()
                        }
                    )
                }
            }
        }

        // Line 2 (29dp): Guide line continuing + Content preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(JournalRuleSpacing)
                .padding(start = 14.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Guide line segment
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(JournalRuleSpacing)
                    .drawBehind {
                        if (!isLastInGroup) {
                            val centerX = size.width / 2f
                            drawLine(
                                color = guideLineColor,
                                start = Offset(centerX, 0f),
                                end = Offset(centerX, size.height),
                                strokeWidth = 1.6.dp.toPx()
                            )
                        }
                    }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Preview Text sitting on line 2
            Text(
                text = previewContent.ifBlank { if (isRtl) "لا يوجد محتوى..." else "Aucun contenu..." },
                fontFamily = resolveJournalFont(previewContent, isRtl),
                fontSize = if (isArabicScript(previewContent)) 13.5.sp else 14.sp,
                color = JournalMutedInk.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(platformStyle = NoFontPadding),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
