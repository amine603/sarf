package com.cash.guide.feature.note

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Palette for left crescent moon accent: Soft Red, Blue, Olive, Amber, Violet, Rose
    val cardAccentColors = remember {
        listOf(
            Color(0xFFE26D6D), // Soft Red / Coral
            Color(0xFF4A89DC), // Soft Blue
            Color(0xFF6B9E48), // Olive Sage
            Color(0xFFE59834), // Warm Amber
            Color(0xFF8E5EA2), // Soft Violet
            Color(0xFFE26D8B)  // Soft Rose
        )
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
            // Header Top Bar: Back button on start, centered Title in Pink Pill (16dp rounded), symmetrical spacer
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
                        .height(50.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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

                    // Centered Title in Watercolor Pink Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(HighlighterPink.copy(alpha = 0.35f))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val titleText = if (isRtl) "ملاحظاتي وأفكاري" else "Mes Notes & Idées"
                        Text(
                            text = titleText,
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = if (isRtl) 17.sp else 17.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalWritingInk,
                            textAlign = TextAlign.Center,
                            style = TextStyle(platformStyle = NoFontPadding)
                        )
                    }

                    // Symmetrical Spacer to guarantee dead-center title alignment
                    Spacer(modifier = Modifier.size(42.dp))
                }
            }

            // Ruled Paper Content
            JournalRuledDocument(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                clearFocusOnTap = true
            ) {
                // Rule 1: 1-rule blank space (29dp) between header and search bar
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Rule 2: Search Bar + Calendar Icon directly resting on the blue line (29dp)
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
                            .background(Color(0xFF6B7067).copy(alpha = 0.16f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                searchFocusRequester.requestFocus()
                                keyboardController?.show()
                            }
                            .padding(horizontal = 9.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            HisabiSketchIcon(
                                symbol = HisabiSymbol.Search,
                                contentDescription = null,
                                tint = JournalInk.copy(alpha = 0.70f),
                                size = 16.dp
                            )

                            val searchPlaceholder = if (isRtl) "بحث في الملاحظات..." else "Rechercher une note..."
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
                                    fontSize = if (isRtl) 15.sp else 16.sp,
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
                                            color = JournalWritingInk.copy(alpha = 0.60f),
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

                    // Calendar icon button sitting directly on the blue line (29dp height, larger icon)
                    Spacer(modifier = Modifier.width(14.dp))
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
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Calendar,
                            contentDescription = stringResource(R.string.home_pick_date),
                            tint = if (uiState.selectedMonthKey != null) JournalInk else JournalInk.copy(alpha = 0.85f),
                            size = 25.dp
                        )
                    }
                }

                // Rule 3: Info Row (3 notes sitting directly on the blue line) (29dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isRtl) "${uiState.totalCount} ملاحظات" else "${uiState.totalCount} notes",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.journalBaselineOnRule()
                    )

                    if (uiState.selectedMonthKey != null) {
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
                    val allNotes = remember(uiState.monthGroups) {
                        uiState.monthGroups.flatMap { it.notes }
                    }

                    uiState.monthGroups.forEach { monthGroup ->
                        // Month Header Divider if multiple months exist and no month filter active (29dp)
                        if (uiState.monthGroups.size > 1 && uiState.selectedMonthKey == null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JournalRuleSpacing)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = monthGroup.displayTitle,
                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                    fontSize = if (isRtl) 15.sp else 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalWritingInk.copy(alpha = 0.85f),
                                    style = TextStyle(platformStyle = NoFontPadding),
                                    modifier = Modifier.journalBaselineOnRule()
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(JournalRuleSpacing)
                                        .drawBehind {
                                            val y = size.height
                                            drawLine(
                                                color = JournalRule.copy(alpha = 0.40f),
                                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                        }
                                )
                            }
                            Spacer(modifier = Modifier.height(JournalRuleSpacing))
                        }

                        // Full-Pill Creamy Cards (Height = 58dp = 2 rule boxes, R = 29dp, right-pointing crescent on left)
                        monthGroup.notes.forEach { note ->
                            val globalIndex = allNotes.indexOf(note).coerceAtLeast(0)
                            val accentColor = cardAccentColors[globalIndex % cardAccentColors.size]
                            val dateStr = dateFormatter.format(Date(note.updatedAtEpochMs))
                            val displayTitle = note.title.ifBlank { if (isRtl) "ملاحظة بدون عنوان" else "Note sans titre" }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JournalRuleSpacing * 2) // exactly 58dp = 2 rule boxes!
                                    .padding(horizontal = 14.dp)
                                    .shadow(
                                        elevation = 1.5.dp,
                                        shape = RoundedCornerShape(29.dp),
                                        ambientColor = Color(0x12000000),
                                        spotColor = Color(0x08000000)
                                    )
                                    .clip(RoundedCornerShape(29.dp))
                                    .clickable(role = Role.Button) { onOpenNote(note.id) },
                                shape = RoundedCornerShape(29.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFAF4)),
                                border = BorderStroke(0.9.dp, Color(0xFFD6CEBF))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            // Crescent moon on left: horns point to the right as in user sketch
                                            val hornX = 46.dp.toPx()
                                            val hollowX = 10.dp.toPx()
                                            val crescentPath = Path().apply {
                                                moveTo(0f, 0f)
                                                lineTo(hornX, 0f)
                                                cubicTo(
                                                    hollowX, size.height * 0.25f,
                                                    hollowX, size.height * 0.75f,
                                                    hornX, size.height
                                                )
                                                lineTo(0f, size.height)
                                                close()
                                            }
                                            drawPath(path = crescentPath, color = accentColor)
                                        }
                                        .padding(start = 52.dp, end = 20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Title + Date in vertical center
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = displayTitle,
                                                fontFamily = resolveJournalFont(displayTitle, isRtl),
                                                fontSize = if (isArabicScript(displayTitle)) 16.5.sp else 17.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JournalWritingInk,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Start,
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = dateStr,
                                                fontFamily = PatrickHandFamily,
                                                fontSize = 11.5.sp,
                                                color = JournalMutedInk.copy(alpha = 0.65f),
                                                style = TextStyle(platformStyle = NoFontPadding)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        // Right Entry Arrow vertically centered in the right semicircular cap
                                        Text(
                                            text = "→",
                                            fontFamily = PatrickHandFamily,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalMutedInk.copy(alpha = 0.65f)
                                        )
                                    }
                                }
                            }

                            // 1 empty box (29dp) between cards so every card aligns to the blue rules!
                            Spacer(modifier = Modifier.height(JournalRuleSpacing))
                        }
                    }
                }

                // 1-rule spacer before primary action button
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Primary Action Button: "+ Nouvelle note" / "+ ملاحظة جديدة" (29dp)
                NotebookPrimaryActionButton(
                    text = if (isRtl) "ملاحظة جديدة" else "Nouvelle note",
                    onClick = {
                        coroutineScope.launch {
                            val newId = viewModel.createNewNote()
                            onOpenNote(newId)
                        }
                    }
                )

                // Bottom extra breathing room
                Spacer(modifier = Modifier.height(JournalRuleSpacing * 3))
            }
        }

        // Month Picker Dialog Popup matching Homepage
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
