package com.cash.guide.feature.note

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import com.cash.guide.data.db.NoteEntity
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.NoFontPadding
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
import com.cash.guide.ui.notebook.NotebookSearchField
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.resolveJournalFont
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun NotesOverviewScreen(
    viewModel: NotesOverviewViewModel,
    onNavigateBack: () -> Unit,
    onOpenNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    var showMonthFilterDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("d MMM yyyy • HH:mm", Locale.getDefault()) }

    // Clear focus when opening screen so soft keyboard doesn't pop up
    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
    }

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
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back Button (Left)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .clickable(role = Role.Button) { onNavigateBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                HisabiSketchIcon(
                                    symbol = HisabiSymbol.Back,
                                    contentDescription = "Retour",
                                    tint = JournalInk,
                                    size = 20.dp
                                )
                            }

                            // Centered Title Pill
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val pageTitle = if (isRtl) "ملاحظاتي وأفكاري" else "Mes Notes & Idées"
                                    Text(
                                        text = pageTitle,
                                        fontFamily = resolveJournalFont(pageTitle, isRtl),
                                        fontSize = if (isRtl) 17.5.sp else 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JournalWritingInk,
                                        style = TextStyle(platformStyle = NoFontPadding)
                                    )
                                    // Total count badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HighlighterPink.copy(alpha = 0.40f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${uiState.totalCount}",
                                            fontFamily = PatrickHandFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JournalWritingInk
                                        )
                                    }
                                }
                            }

                            // Top Right: Calendar button for filtering by Month
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
                    }

                    // Active Month Filter Banner (if a specific month is selected)
                    if (uiState.selectedMonthKey != null) {
                        val activeMonthDisplay = uiState.availableMonths
                            .firstOrNull { it.first == uiState.selectedMonthKey }?.second ?: uiState.selectedMonthKey
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(HighlighterYellow.copy(alpha = 0.25f))
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "📅",
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (isRtl) "تصفية حسب: $activeMonthDisplay" else "Filtré par : $activeMonthDisplay",
                                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }

                            // Clear filter button
                            Text(
                                text = if (isRtl) "إلغاء ✕" else "Tout voir ✕",
                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.clickable { viewModel.selectMonth(null) }
                            )
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

            // Notebook ruled list
            JournalRuledDocument(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                clearFocusOnTap = true
            ) {
                // Search field placed right at the top
                NotebookSearchField(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    placeholder = if (isRtl) "بحث في الملاحظات..." else "Rechercher une note..."
                )

                // 1 Rule spacer between search and notes
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Empty State
                if (uiState.monthGroups.isEmpty() && !uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JournalRuleSpacing * 8)
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
                            text = if (isRtl) "لا توجد أي ملاحظات" else "Aucune note trouvée",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalMutedInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRtl) "اضغط على الزر الدائري (+) لتحت لإضافة ملاحظة جديدة ✍️" else "Appuyez sur le bouton circulaire (+) en bas pour ajouter une note ✍️",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 13.5.sp,
                            color = JournalMutedInk.copy(alpha = 0.70f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Month-grouped cards list: Note under note with outline, transparent background, text on blue lines
                    uiState.monthGroups.forEach { monthGroup ->
                        // Month Header Band (1 exact rule: 29dp)
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
                                modifier = Modifier.journalBaselineOnRule(opticalOffsetFromBottom = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(JournalRuleSpacing)
                                    .drawBehind {
                                        val y = size.height
                                        drawLine(
                                            color = JournalRule.copy(alpha = 0.50f),
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 1.2.dp.toPx()
                                        )
                                    }
                            )
                        }

                        // 1 exact rule spacer
                        Spacer(modifier = Modifier.height(JournalRuleSpacing))

                        // Cards list for this month
                        monthGroup.notes.forEach { note ->
                            NoteCardItem(
                                note = note,
                                isRtl = isRtl,
                                formattedDate = dateFormatter.format(Date(note.updatedAtEpochMs)),
                                onClick = { onOpenNote(note.id) },
                                onTogglePin = { viewModel.togglePin(note.id, !note.isPinned) },
                                onDelete = { noteToDelete = note }
                            )

                            // 1 exact rule spacer between cards (29dp) so the blue lines never drift
                            Spacer(modifier = Modifier.height(JournalRuleSpacing))
                        }
                    }
                }

                // Clearance for bottom floating button
                Spacer(modifier = Modifier.height(JournalRuleSpacing * 4))
            }
        }

        // Circular Floating Action Button (+) for New Note (Bottom-Right)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp)
                        .navigationBarsPadding()
                        .size(56.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(JournalWritingInk)
                        .clickable(role = Role.Button) {
                            coroutineScope.launch {
                                val newId = viewModel.createNewNote()
                                onOpenNote(newId)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Plus,
                        contentDescription = if (isRtl) "ملاحظة جديدة" else "Nouvelle note",
                        tint = Color.White,
                        size = 26.dp
                    )
                }
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
                        // "Tous les mois" option
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

                        // Available Months
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

/**
 * Individual Note Card:
 * - Transparent background (Color.Transparent) so blue lines pass seamlessly through!
 * - Single outline border colored distinctly per note (Yellow, Pink, Blue, Green, Purple, or rotating vibrant ink colors).
 * - Total height is exactly 3 notebook rules (87dp).
 * - Each row of text sits directly on its blue line using journalBaselineOnRule.
 */
@Composable
fun NoteCardItem(
    note: NoteEntity,
    isRtl: Boolean,
    formattedDate: String,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine outline color: use note's colorTag or rotating vibrant ink palette
    val defaultInkPalettes = remember {
        listOf(
            Color(0xFF2563EB), // Notebook Royal Blue
            Color(0xFFE11D48), // Rose Ink
            Color(0xFF059669), // Emerald Ink
            Color(0xFFD97706), // Amber Ink
            Color(0xFF7C3AED), // Violet Ink
            Color(0xFF0D9488), // Teal Ink
            Color(0xFFC2410C)  // Terracotta Ink
        )
    }

    val outlineColor = when (note.colorTag.uppercase()) {
        "YELLOW" -> Color(0xFFD97706)
        "PINK" -> Color(0xFFE11D48)
        "BLUE" -> Color(0xFF2563EB)
        "GREEN" -> Color(0xFF059669)
        "PURPLE" -> Color(0xFF7C3AED)
        else -> {
            val idx = (note.id.hashCode().absoluteValue) % defaultInkPalettes.size
            defaultInkPalettes[idx]
        }
    }

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .border(BorderStroke(1.4.dp, outlineColor), shape)
            .clip(shape)
            .background(Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
        ) {
            // Rule 1 (29dp): Pin icon + Title + Action icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (note.isPinned) {
                        Text(
                            text = "📌",
                            fontSize = 13.sp,
                            modifier = Modifier.journalBaselineOnRule(opticalOffsetFromBottom = 2.dp)
                        )
                    }
                    val displayTitle = note.title.ifBlank { if (isRtl) "ملاحظة بدون عنوان" else "Note sans titre" }
                    Text(
                        text = displayTitle,
                        fontFamily = resolveJournalFont(displayTitle, isRtl),
                        fontSize = if (isRtl) 16.5.sp else 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalWritingInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.journalBaselineOnRule(opticalOffsetFromBottom = 2.dp)
                    )
                }

                // Action buttons: Pin toggle + Trash
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pin toggle
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onTogglePin)
                            .journalBaselineOnRule(opticalOffsetFromBottom = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (note.isPinned) "📌" else "📍",
                            fontSize = 13.sp
                        )
                    }

                    // Delete trash
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(role = Role.Button, onClick = onDelete)
                            .journalBaselineOnRule(opticalOffsetFromBottom = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Trash,
                            contentDescription = "Supprimer",
                            tint = JournalActionDelete.copy(alpha = 0.75f),
                            size = 14.dp
                        )
                    }
                }
            }

            // Rule 2 (29dp): Note content preview sitting directly on Rule 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing),
                verticalAlignment = Alignment.Bottom
            ) {
                val preview = note.content.trim().ifBlank {
                    if (isRtl) "ملاحظة فارغة..." else "Note vide..."
                }
                Text(
                    text = preview,
                    fontFamily = resolveJournalFont(preview, isRtl),
                    fontSize = if (isRtl) 14.sp else 14.5.sp,
                    color = JournalInk.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule(opticalOffsetFromBottom = 2.dp)
                )
            }

            // Rule 3 (29dp): Date & Time sitting directly on Rule 3
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(JournalRuleSpacing),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = formattedDate,
                    fontFamily = PatrickHandFamily,
                    fontSize = 12.sp,
                    color = JournalMutedInk.copy(alpha = 0.70f),
                    style = TextStyle(platformStyle = NoFontPadding),
                    modifier = Modifier.journalBaselineOnRule(opticalOffsetFromBottom = 2.dp)
                )
            }
        }
    }
}
