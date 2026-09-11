package com.cash.guide.feature.note

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.data.db.NoteEntity
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
    onOpenNote: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val coroutineScope = rememberCoroutineScope()

    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JournalPaper)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = JournalPaper,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row: Back button + Title in Pink Pill + Spacer
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
                            Text(
                                text = if (isRtl) "ملاحظاتي وأفكاري" else "Mes Notes & Idées",
                                fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                fontSize = if (isRtl) 17.sp else 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk,
                                textAlign = TextAlign.Center,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }

                        // Balancing Spacer
                        Spacer(modifier = Modifier.size(42.dp))
                    }

                    // Sub-row: count badge
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
                // Top spacer: 1 exact notebook rule
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Search field
                NotebookSearchField(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    placeholder = if (isRtl) "بحث في الملاحظات..." else "Rechercher une note...",
                    onOpenCalendar = null,
                    isDateFiltered = false
                )

                // Spacer
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // New Note primary action button
                NotebookPrimaryActionButton(
                    text = if (isRtl) "ملاحظة جديدة" else "Nouvelle note",
                    onClick = {
                        coroutineScope.launch {
                            val newId = viewModel.createNewNote()
                            onOpenNote(newId)
                        }
                    }
                )

                // Spacer
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
                            size = 36.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isRtl) "لا توجد أي ملاحظات حالياً" else "Aucune note pour l'instant",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalMutedInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isRtl) "قيد أفكارك، خواطرك، أو أي حاجة بغيتي تفكرها ✍️" else "Notez vos idées, pensées et mémos du quotidien ✍️",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 13.5.sp,
                            color = JournalMutedInk.copy(alpha = 0.70f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Month-grouped cards list: Note under note with outline
                    uiState.monthGroups.forEach { monthGroup ->
                        // Month Header Band
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
                                            color = JournalRule.copy(alpha = 0.50f),
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 1.2.dp.toPx()
                                        )
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Bottom extra space
                Spacer(modifier = Modifier.height(JournalRuleSpacing * 2))
            }
        }

        // Delete Confirmation Dialog
        noteToDelete?.let { note ->
            AlertDialog(
                onDismissRequest = { noteToDelete = null },
                title = {
                    Text(
                        text = if (isRtl) "مسح الملاحظة؟" else "Supprimer cette note ?",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalWritingInk
                    )
                },
                text = {
                    Text(
                        text = if (isRtl) {
                            "واش متأكد باغي تمسح \"${note.title.ifBlank { "هاد الملاحظة" }}\"؟"
                        } else {
                            "Voulez-vous vraiment supprimer \"${note.title.ifBlank { "cette note" }}\" ?"
                        },
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 14.5.sp,
                        color = JournalMutedInk
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteNote(note.id)
                            noteToDelete = null
                        }
                    ) {
                        Text(
                            text = if (isRtl) "مسح" else "Supprimer",
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
 * Individual Note Card with outline and subtle paper tint.
 * Stacked vertically (note ta7t note).
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
    val cardColors = getNoteCardColors(note.colorTag)
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(shape)
            .background(cardColors.background)
            .border(
                BorderStroke(1.2.dp, cardColors.outline),
                shape
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        // Top row: Pin indicator + Title + Date & Time + Trash
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (note.isPinned) {
                    Text(
                        text = "📌",
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = note.title.ifBlank { if (isRtl) "ملاحظة بدون عنوان" else "Note sans titre" },
                    fontFamily = resolveJournalFont(note.title, isRtl),
                    fontSize = if (isRtl) 16.5.sp else 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalWritingInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Date & Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = formattedDate,
                    fontFamily = PatrickHandFamily,
                    fontSize = 12.sp,
                    color = JournalMutedInk.copy(alpha = 0.70f)
                )

                // Pin toggle icon
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onTogglePin),
                    contentAlignment = Alignment.Center
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Pin,
                        contentDescription = "Épingler",
                        tint = if (note.isPinned) Color(0xFFE11D48) else JournalMutedInk.copy(alpha = 0.45f),
                        size = 14.dp
                    )
                }

                // Delete button
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onDelete),
                    contentAlignment = Alignment.Center
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Trash,
                        contentDescription = "Supprimer",
                        tint = JournalActionDelete.copy(alpha = 0.65f),
                        size = 14.dp
                    )
                }
            }
        }

        // Content snippet (2-3 lines with ellipsis)
        if (note.content.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = note.content,
                fontFamily = resolveJournalFont(note.content, isRtl),
                fontSize = if (isRtl) 14.sp else 14.5.sp,
                fontWeight = FontWeight.Normal,
                color = JournalInk.copy(alpha = 0.85f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = if (isRtl) 20.sp else 19.sp
            )
        }
    }
}

data class NoteCardColors(
    val background: Color,
    val outline: Color
)

fun getNoteCardColors(colorTag: String): NoteCardColors {
    return when (colorTag.uppercase()) {
        "YELLOW" -> NoteCardColors(
            background = Color(0xFFFFFBEB),
            outline = Color(0xFFFDE68A)
        )
        "PINK" -> NoteCardColors(
            background = Color(0xFFFDF2F8),
            outline = Color(0xFFFBCFE8)
        )
        "BLUE" -> NoteCardColors(
            background = Color(0xFFF0F9FF),
            outline = Color(0xFFBAE6FD)
        )
        "GREEN" -> NoteCardColors(
            background = Color(0xFFF0FDF4),
            outline = Color(0xFFBBF7D0)
        )
        "PURPLE" -> NoteCardColors(
            background = Color(0xFFFAF5FF),
            outline = Color(0xFFE9D5FF)
        )
        else -> NoteCardColors(
            background = Color(0xFFFFFFFD),
            outline = JournalRule.copy(alpha = 0.65f)
        )
    }
}
