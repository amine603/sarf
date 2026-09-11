package com.cash.guide.feature.note

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.cash.guide.ui.notebook.NotebookSearchField
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.isArabicScript
import com.cash.guide.ui.notebook.resolveJournalFont
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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

    var showMonthPicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Soft palette matching the app highlighter & notebook tones (Red, Blue, Olive, Amber, Violet, Rose)
    val cardAccentColors = remember {
        listOf(
            Color(0xFFE26D6D), // Soft Red
            Color(0xFF4A89DC), // Soft Blue
            Color(0xFF6B9E48), // Olive Sage
            Color(0xFFE59834), // Warm Amber
            Color(0xFF8E5EA2), // Soft Violet
            Color(0xFFE26D8B)  // Soft Rose
        )
    }

    // Clear focus when opening screen so soft keyboard doesn't pop up
    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JournalPaper)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Top Bar: Back button on start, centered Title in Pink Pill, symmetrical spacer on end
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
                // Step 1: 1-rule blank space between header and search bar (moving search bar down by 1 step)
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Step 2: Search Bar + Calendar icon on the right (matching Homepage exactly)
                NotebookSearchField(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onOpenCalendar = { showMonthPicker = true },
                    isDateFiltered = uiState.selectedMonthKey != null,
                    placeholder = if (isRtl) "بحث في الملاحظات..." else "Rechercher une note..."
                )

                // Step 3: Small spacer
                Spacer(modifier = Modifier.height(6.dp))

                // Step 4: Info Row directly below Search Bar (Note count on start, Active month tag on end)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isRtl) "${uiState.totalCount} ملاحظات" else "${uiState.totalCount} notes",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding)
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

                // Step 5: Spacer before cards list
                Spacer(modifier = Modifier.height(10.dp))

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
                        // Month Header Divider if multiple months exist and no month filter active
                        if (uiState.monthGroups.size > 1 && uiState.selectedMonthKey == null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = monthGroup.displayTitle,
                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                    fontSize = if (isRtl) 15.sp else 15.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalWritingInk.copy(alpha = 0.85f),
                                    style = TextStyle(platformStyle = NoFontPadding)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(JournalRule.copy(alpha = 0.40f))
                                )
                            }
                        }

                        // Creamy Cards (Height = 64dp: exactly 2 rule lines with generous breathing room)
                        monthGroup.notes.forEach { note ->
                            val globalIndex = allNotes.indexOf(note).coerceAtLeast(0)
                            val accentColor = cardAccentColors[globalIndex % cardAccentColors.size]
                            val dateStr = dateFormatter.format(Date(note.updatedAtEpochMs))
                            val displayTitle = note.title.ifBlank { if (isRtl) "ملاحظة بدون عنوان" else "Note sans titre" }
                            val snippet = note.content.replace('\n', ' ').trim()

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(66.dp)
                                    .padding(horizontal = 14.dp, vertical = 3.5.dp)
                                    .shadow(
                                        elevation = 1.5.dp,
                                        shape = RoundedCornerShape(10.dp),
                                        ambientColor = Color(0x12000000),
                                        spotColor = Color(0x0A000000)
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(role = Role.Button) { onOpenNote(note.id) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFAF4)),
                                border = BorderStroke(0.8.dp, Color(0xFFE8E2D6))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .drawBehind {
                                            // Slender colored arc / capsule on the left edge
                                            val arcWidth = 4.dp.toPx()
                                            val insetY = 7.dp.toPx()
                                            drawRoundRect(
                                                color = accentColor,
                                                topLeft = Offset(0f, insetY),
                                                size = Size(arcWidth, size.height - insetY * 2),
                                                cornerRadius = CornerRadius(arcWidth / 2, arcWidth / 2)
                                            )
                                        }
                                        .padding(start = 14.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Line 1: Note Title + Right Arrow
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = displayTitle,
                                                fontFamily = resolveJournalFont(displayTitle, isRtl),
                                                fontSize = if (isArabicScript(displayTitle)) 16.sp else 16.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JournalWritingInk,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Start,
                                                style = TextStyle(platformStyle = NoFontPadding),
                                                modifier = Modifier.weight(1f)
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Sleek right entry arrow on the far right
                                            Text(
                                                text = "→",
                                                fontFamily = PatrickHandFamily,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JournalMutedInk.copy(alpha = 0.55f)
                                            )
                                        }

                                        // Line 2: Snippet preview + Tiny Date
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            if (snippet.isNotBlank()) {
                                                Text(
                                                    text = snippet,
                                                    fontFamily = resolveJournalFont(snippet, isRtl),
                                                    fontSize = 12.5.sp,
                                                    color = JournalMutedInk.copy(alpha = 0.80f),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Start,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }

                                            Text(
                                                text = dateStr,
                                                fontFamily = PatrickHandFamily,
                                                fontSize = 11.sp,
                                                color = JournalMutedInk.copy(alpha = 0.50f),
                                                modifier = Modifier.padding(start = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Spacer before bottom primary action button
                Spacer(modifier = Modifier.height(14.dp))

                // Primary Action Button: "+ Nouvelle note" / "+ ملاحظة جديدة"
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
