package com.cash.guide.feature.note

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
import com.cash.guide.data.db.NoteEntity
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
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.NotebookPrimaryActionButton
import com.cash.guide.ui.notebook.NotebookSearchField
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.resolveJournalFont
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
    var showMonthFilterDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val rowDotColors = remember {
        listOf(
            Color(0xFF3B82B6), // Soft Blue
            Color(0xFFD65D82), // Rose/Pink
            Color(0xFF5E8C3B), // Olive Green
            Color(0xFFC7881E), // Amber Gold
            Color(0xFF7E5AA8), // Purple
            Color(0xFFCC673B)  // Terracotta
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
            // Header Top Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = JournalPaper,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1: Back button + Title in Pink Pill + Calendar button
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

                        // Calendar button to filter by month
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.selectedMonthKey != null) HighlighterYellow.copy(alpha = 0.65f)
                                    else Color.Transparent
                                )
                                .clickable(role = Role.Button) { showMonthFilterDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            HisabiSketchIcon(
                                symbol = HisabiSymbol.Calendar,
                                contentDescription = "Calendrier",
                                tint = if (uiState.selectedMonthKey != null) Color(0xFFB45309) else JournalInk,
                                size = 20.dp
                            )
                        }
                    }

                    // Row 2: Count badge on start, Active month filter on end
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
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
                                .firstOrNull { it.first == uiState.selectedMonthKey }?.second ?: uiState.selectedMonthKey
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(HighlighterYellow.copy(alpha = 0.40f))
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

                    // Divider separating header from ruled paper
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(JournalRule.copy(alpha = 0.35f))
                    )
                }
            }

            // Ruled Paper List
            JournalRuledDocument(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                clearFocusOnTap = true
            ) {
                // Top Search Field
                NotebookSearchField(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    placeholder = if (isRtl) "بحث في الملاحظات..." else "Rechercher une note..."
                )

                // 1 Rule spacer between search and items
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
                    val allNotes = remember(uiState.monthGroups) {
                        uiState.monthGroups.flatMap { it.notes }
                    }

                    uiState.monthGroups.forEach { monthGroup ->
                        // Month Header Band (1 rule: 29dp) if multiple months exist
                        if (uiState.monthGroups.size > 1 || uiState.selectedMonthKey != null) {
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
                                    fontSize = if (isRtl) 15.sp else 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalWritingInk,
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
                                                color = JournalRule.copy(alpha = 0.45f),
                                                start = Offset(0f, y),
                                                end = Offset(size.width, y),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                        }
                                )
                            }
                            Spacer(modifier = Modifier.height(JournalRuleSpacing))
                        }

                        // Notes items
                        monthGroup.notes.forEach { note ->
                            val globalIndex = allNotes.indexOf(note).coerceAtLeast(0)
                            val dotColor = rowDotColors[globalIndex % rowDotColors.size]
                            val dateStr = dateFormatter.format(Date(note.updatedAtEpochMs))
                            val displayTitle = note.title.ifBlank { if (isRtl) "ملاحظة بدون عنوان" else "Note sans titre" }
                            val snippet = note.content.replace('\n', ' ').trim()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JournalRuleSpacing * 2)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onOpenNote(note.id) }
                                    )
                                    .padding(horizontal = 14.dp)
                            ) {
                                // Rule 1: Number + Title + dotted line + Trash icon
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.widthIn(max = 240.dp),
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "\u200E${globalIndex + 1}.",
                                            fontFamily = PatrickHandFamily,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = dotColor,
                                            style = TextStyle(platformStyle = NoFontPadding),
                                            modifier = Modifier.journalBaselineOnRule()
                                        )
                                        Text(
                                            text = displayTitle,
                                            fontFamily = resolveJournalFont(displayTitle, isRtl),
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalWritingInk,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(platformStyle = NoFontPadding),
                                            modifier = Modifier.journalBaselineOnRule()
                                        )
                                    }

                                    // Subtle connecting dotted line directly on the blue notebook line
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(JournalRuleSpacing)
                                            .padding(horizontal = 6.dp)
                                            .drawBehind {
                                                val strokeW = 0.85.dp.toPx()
                                                val y = size.height
                                                drawLine(
                                                    color = JournalWritingInk.copy(alpha = 0.28f),
                                                    start = Offset(0f, y),
                                                    end = Offset(size.width, y),
                                                    strokeWidth = strokeW,
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.5.dp.toPx()))
                                                )
                                            }
                                    )

                                    // Trash icon button
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable(
                                                role = Role.Button,
                                                onClickLabel = "Supprimer cette note",
                                                onClick = { noteToDelete = note }
                                            )
                                            .journalBaselineOnRule(opticalOffsetFromBottom = 0.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        HisabiSketchIcon(
                                            symbol = HisabiSymbol.Trash,
                                            contentDescription = "Supprimer",
                                            tint = JournalActionDelete.copy(alpha = 0.65f),
                                            size = 16.dp
                                        )
                                    }
                                }

                                // Rule 2: Content preview + Date
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(JournalRuleSpacing),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (snippet.isNotBlank()) {
                                        Text(
                                            text = snippet,
                                            fontFamily = resolveJournalFont(snippet, isRtl),
                                            fontSize = 14.sp,
                                            color = JournalMutedInk.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = TextStyle(platformStyle = NoFontPadding),
                                            modifier = Modifier
                                                .padding(start = 22.dp)
                                                .weight(1f, fill = false)
                                                .journalBaselineOnRule()
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Text(
                                        text = dateStr,
                                        fontFamily = PatrickHandFamily,
                                        fontSize = 12.5.sp,
                                        color = JournalMutedInk.copy(alpha = 0.60f),
                                        style = TextStyle(platformStyle = NoFontPadding),
                                        modifier = Modifier
                                            .then(if (snippet.isBlank()) Modifier.padding(start = 22.dp) else Modifier)
                                            .journalBaselineOnRule()
                                    )
                                }
                            }

                            // 1-rule spacer between note items
                            Spacer(modifier = Modifier.height(JournalRuleSpacing))
                        }
                    }
                }

                // 1-rule spacer before bottom action button
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Bottom Action Button: "+ Nouvelle note" / "+ ملاحظة جديدة"
                NotebookPrimaryActionButton(
                    text = if (isRtl) "ملاحظة جديدة" else "Nouvelle note",
                    onClick = {
                        coroutineScope.launch {
                            val newId = viewModel.createNewNote()
                            onOpenNote(newId)
                        }
                    }
                )

                // Extra breathing room at bottom
                Spacer(modifier = Modifier.height(JournalRuleSpacing * 3))
            }
        }

        // Month Picker Dialog
        if (showMonthFilterDialog) {
            AlertDialog(
                onDismissRequest = { showMonthFilterDialog = false },
                title = {
                    Text(
                        text = if (isRtl) "تصفية الملاحظات حسب الشهر" else "Filtrer par mois",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalWritingInk
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isAllSelected = uiState.selectedMonthKey == null
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllSelected) HighlighterPink.copy(alpha = 0.35f) else Color.Transparent)
                                .clickable {
                                    viewModel.selectMonth(null)
                                    showMonthFilterDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isRtl) "جميع الشهور" else "Tous les mois",
                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                fontSize = 15.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                                color = JournalInk
                            )
                            if (isAllSelected) {
                                Text(text = "✓", fontWeight = FontWeight.Bold, color = JournalWritingInk)
                            }
                        }

                        uiState.availableMonths.forEach { (key, displayTitle) ->
                            val isSelected = uiState.selectedMonthKey == key
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) HighlighterPink.copy(alpha = 0.35f) else Color.Transparent)
                                    .clickable {
                                        viewModel.selectMonth(key)
                                        showMonthFilterDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = displayTitle,
                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = JournalInk
                                )
                                if (isSelected) {
                                    Text(text = "✓", fontWeight = FontWeight.Bold, color = JournalWritingInk)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showMonthFilterDialog = false }) {
                        Text(
                            text = if (isRtl) "إغلاق" else "Fermer",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            color = JournalWritingInk
                        )
                    }
                },
                containerColor = JournalPaper
            )
        }

        // Delete Confirmation Dialog
        noteToDelete?.let { target ->
            val noteTitle = target.title.ifBlank { if (isRtl) "الملاحظة" else "cette note" }
            AlertDialog(
                onDismissRequest = { noteToDelete = null },
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
                            viewModel.deleteNote(target.id)
                            noteToDelete = null
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
                    TextButton(onClick = { noteToDelete = null }) {
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
