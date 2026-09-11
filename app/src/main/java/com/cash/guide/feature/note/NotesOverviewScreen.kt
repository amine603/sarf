package com.cash.guide.feature.note

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.data.db.NoteEntity
import com.cash.guide.ui.notebook.DeleteConfirmationDialog
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

    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var showMonthPicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val cardAccentColors = remember {
        listOf(
            Color(0xFF3B82B6), // Soft Blue
            Color(0xFFE26D8B), // Soft Rose
            Color(0xFF5E8C3B), // Olive Sage
            Color(0xFFD97706), // Warm Amber
            Color(0xFF7E5AA8), // Soft Purple
            Color(0xFFCC673B)  // Terracotta Coral
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
            // Header Top Bar (Back button on start, centered Title in Pink Pill, symmetry spacer on end)
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

                    // Symmetrical Spacer to guarantee dead-center alignment of the Title
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
                // Line 1: Spacer after header
                Spacer(modifier = Modifier.height(4.dp))

                // Line 2: Search Bar exactly matching Homepage (Capsule with calendar icon on the right)
                NotebookSearchField(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onOpenCalendar = { showMonthPicker = true },
                    isDateFiltered = uiState.selectedMonthKey != null,
                    placeholder = if (isRtl) "بحث في الملاحظات..." else "Rechercher une note..."
                )

                // Line 3: Small spacer
                Spacer(modifier = Modifier.height(6.dp))

                // Line 4: Info Row directly below Search Bar (Note count on start, Active month tag on end)
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

                // Line 5: Spacer before cards list
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
                        // Month Header Divider if multiple months exist and no month filter
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

                        // Creative Note Cards List
                        monthGroup.notes.forEach { note ->
                            val globalIndex = allNotes.indexOf(note).coerceAtLeast(0)
                            val accentColor = cardAccentColors[globalIndex % cardAccentColors.size]
                            val dateStr = dateFormatter.format(Date(note.updatedAtEpochMs))
                            val displayTitle = note.title.ifBlank { if (isRtl) "ملاحظة بدون عنوان" else "Note sans titre" }
                            val snippet = note.content.replace('\n', ' ').trim()
                            val titleIsArabic = isArabicScript(displayTitle)
                            val snippetIsArabic = isArabicScript(snippet)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                                    .shadow(
                                        elevation = 2.dp,
                                        shape = RoundedCornerShape(14.dp),
                                        ambientColor = Color(0x18000000),
                                        spotColor = Color(0x10000000)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(role = Role.Button) { onOpenNote(note.id) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
                                border = BorderStroke(0.9.dp, Color(0xFFE8E2D6))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .drawBehind {
                                            // Decorative colored marker strip on the leading edge
                                            val barWidth = 4.5.dp.toPx()
                                            if (isRtl) {
                                                drawRoundRect(
                                                    color = accentColor,
                                                    topLeft = Offset(size.width - barWidth, 0f),
                                                    size = Size(barWidth, size.height),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                            } else {
                                                drawRoundRect(
                                                    color = accentColor,
                                                    topLeft = Offset(0f, 0f),
                                                    size = Size(barWidth, size.height),
                                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                                )
                                            }
                                        }
                                        .padding(
                                            start = if (isRtl) 14.dp else 16.dp,
                                            end = if (isRtl) 16.dp else 14.dp,
                                            top = 11.dp,
                                            bottom = 10.dp
                                        )
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Card Header: Badge + Title + Trash
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Number badge pill
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(accentColor.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "#${globalIndex + 1}",
                                                        fontFamily = PatrickHandFamily,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = accentColor
                                                    )
                                                }

                                                // Note Title
                                                Text(
                                                    text = displayTitle,
                                                    fontFamily = resolveJournalFont(displayTitle, isRtl),
                                                    fontSize = if (titleIsArabic) 16.5.sp else 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = JournalWritingInk,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = if (titleIsArabic) TextAlign.Right else TextAlign.Left,
                                                    style = TextStyle(platformStyle = NoFontPadding),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            // Trash Button
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .clickable(
                                                        role = Role.Button,
                                                        onClickLabel = "Supprimer",
                                                        onClick = { noteToDelete = note }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                HisabiSketchIcon(
                                                    symbol = HisabiSymbol.Trash,
                                                    contentDescription = "Supprimer",
                                                    tint = JournalActionDelete.copy(alpha = 0.65f),
                                                    size = 16.dp
                                                )
                                            }
                                        }

                                        // Card Body: Snippet Preview
                                        Spacer(modifier = Modifier.height(6.dp))
                                        if (snippet.isNotBlank()) {
                                            Text(
                                                text = snippet,
                                                fontFamily = resolveJournalFont(snippet, isRtl),
                                                fontSize = if (snippetIsArabic) 13.5.sp else 14.sp,
                                                color = JournalInk.copy(alpha = 0.75f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 19.sp,
                                                textAlign = if (snippetIsArabic) TextAlign.Right else TextAlign.Left,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            Text(
                                                text = if (isRtl) "لا يوجد نص إضافي..." else "Aucun contenu écrit...",
                                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                fontSize = 13.sp,
                                                fontStyle = FontStyle.Italic,
                                                color = JournalMutedInk.copy(alpha = 0.50f)
                                            )
                                        }

                                        // Card Footer: Subtle Divider & Meta Info
                                        Spacer(modifier = Modifier.height(9.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(0.6.dp)
                                                .background(Color(0xFFEDE7DB))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                HisabiSketchIcon(
                                                    symbol = HisabiSymbol.Clock,
                                                    contentDescription = null,
                                                    tint = JournalMutedInk.copy(alpha = 0.60f),
                                                    size = 12.dp
                                                )
                                                Text(
                                                    text = dateStr,
                                                    fontFamily = PatrickHandFamily,
                                                    fontSize = 12.sp,
                                                    color = JournalMutedInk.copy(alpha = 0.70f)
                                                )
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFF6F3EC))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = if (isRtl) "فتح ←" else "Ouvrir →",
                                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = JournalWritingInk.copy(alpha = 0.70f)
                                                )
                                            }
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

        // Delete Confirmation Dialog matching Notebook style
        if (noteToDelete != null) {
            DeleteConfirmationDialog(
                onConfirmDelete = {
                    coroutineScope.launch {
                        noteToDelete?.let { viewModel.deleteNote(it.id) }
                        noteToDelete = null
                    }
                },
                onDismiss = { noteToDelete = null }
            )
        }
    }
}
