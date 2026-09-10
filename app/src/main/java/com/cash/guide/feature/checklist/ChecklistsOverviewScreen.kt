package com.cash.guide.feature.checklist

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.resolveJournalFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ColorEmerald = Color(0xFF1B7A4B)
private val ColorCoral = Color(0xFFD9534F)

@Composable
fun ChecklistsOverviewScreen(
    viewModel: ChecklistsOverviewViewModel,
    onOpenChecklist: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val checklists by viewModel.checklists.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    val rowDotColors = remember {
        listOf(
            Color(0xFF3B82B6),
            Color(0xFFD65D82),
            Color(0xFF5E8C3B),
            Color(0xFFC7881E),
            Color(0xFF7E5AA8),
            Color(0xFFCC673B)
        )
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

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
                                text = if (isRtl) "قوائم المهام" else "Mes Checklists",
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
                            text = if (isRtl) "${checklists.size} قوائم" else "${checklists.size} listes",
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
                // Top spacer: 1 exact notebook rule (29dp)
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Empty State
                if (checklists.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(JournalRuleSpacing * 5)
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HisabiSketchIcon(
                            symbol = HisabiSymbol.Page,
                            contentDescription = null,
                            tint = JournalMutedInk.copy(alpha = 0.40f),
                            size = 36.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isRtl) "لا توجد أي قائمة حالياً" else "Aucune checklist pour l'instant",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalMutedInk
                        )
                        Text(
                            text = if (isRtl) "اضغط على الزر أسفله لإنشاء قائمتك الأولى ✍️" else "Créez votre première liste de courses ou tâches ✍️",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 13.5.sp,
                            color = JournalMutedInk.copy(alpha = 0.70f)
                        )
                    }
                } else {
                    // Checklists List
                    checklists.forEachIndexed { index, item ->
                        val dotColor = rowDotColors[index % rowDotColors.size]
                        val total = item.totalCount
                        val completed = item.completedCount
                        val isDone = total > 0 && completed == total
                        val dateStr = dateFormatter.format(Date(item.checklist.createdAtEpochMs))

                        // Spanning 2 notebook rules per checklist card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(JournalRuleSpacing * 2)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onOpenChecklist(item.checklist.id) }
                                )
                                .padding(horizontal = 14.dp)
                        ) {
                            // Line 1: Number + Title on start, Date on end
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JournalRuleSpacing),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        fontFamily = PatrickHandFamily,
                                        fontSize = 16.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = dotColor,
                                        style = TextStyle(platformStyle = NoFontPadding),
                                        modifier = Modifier.journalBaselineOnRule()
                                    )
                                    Text(
                                        text = item.checklist.title.ifBlank { "Checklist" },
                                        fontFamily = resolveJournalFont(item.checklist.title, isRtl),
                                        fontSize = 16.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JournalWritingInk,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = TextStyle(platformStyle = NoFontPadding),
                                        modifier = Modifier.journalBaselineOnRule()
                                    )
                                }

                                Text(
                                    text = dateStr,
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 12.sp,
                                    color = JournalMutedInk.copy(alpha = 0.65f),
                                    style = TextStyle(platformStyle = NoFontPadding),
                                    modifier = Modifier.journalBaselineOnRule()
                                )
                            }

                            // Line 2: Status / Progress on start, Delete button on end
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(JournalRuleSpacing),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Progress Badge / Text
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(start = 22.dp)
                                ) {
                                    if (total == 0) {
                                        Text(
                                            text = if (isRtl) "فارغة (0 عنصر)" else "Vide (0 élément)",
                                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                            fontSize = 13.sp,
                                            color = JournalMutedInk.copy(alpha = 0.70f),
                                            style = TextStyle(platformStyle = NoFontPadding),
                                            modifier = Modifier.journalBaselineOnRule()
                                        )
                                    } else {
                                        Text(
                                            text = if (isDone) {
                                                if (isRtl) "✓ مكتملة ($total/$total)" else "✓ Terminée ($total/$total)"
                                            } else {
                                                if (isRtl) "$completed من $total متبقي" else "$completed / $total faits"
                                            },
                                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                            fontSize = 13.sp,
                                            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isDone) ColorEmerald else JournalWritingInk,
                                            style = TextStyle(platformStyle = NoFontPadding),
                                            modifier = Modifier.journalBaselineOnRule()
                                        )
                                    }
                                }

                                // Delete checklist icon button
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable(
                                            role = Role.Button,
                                            onClickLabel = "Supprimer cette checklist",
                                            onClick = { viewModel.promptDeleteChecklist(item) }
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
                        }
                    }
                }

                // 1-rule spacer before bottom button
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Bottom Action: "+ Nouvelle checklist"
                NotebookPrimaryActionButton(
                    text = if (isRtl) "قائمة جديدة" else "Nouvelle checklist",
                    onClick = { viewModel.openCreateDialog() }
                )

                // Extra breathing room at bottom
                Spacer(modifier = Modifier.height(JournalRuleSpacing * 3))
            }
        }
    }

    // Create Checklist Dialog
    if (uiState.showCreateDialog) {
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissCreateDialog() },
            containerColor = JournalPaper,
            title = {
                val dialogTitle = if (isRtl) "قائمة جديدة" else "Nouvelle checklist"
                Text(
                    text = dialogTitle,
                    fontFamily = resolveJournalFont(dialogTitle, isRtl),
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isRtl) "اسم القائمة (مثال: سخرة، خضار، مقاضي...)" else "Nom de la checklist (ex: Skhra, Supermarché...)",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 13.sp,
                        color = JournalMutedInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BasicTextField(
                        value = uiState.newChecklistTitle,
                        onValueChange = { viewModel.setNewChecklistTitle(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .drawBehind {
                                val strokeW = 1.2.dp.toPx()
                                val y = size.height
                                drawLine(
                                    color = JournalInk.copy(alpha = 0.6f),
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }
                            .padding(vertical = 4.dp),
                        singleLine = true,
                        cursorBrush = SolidColor(JournalInk),
                        textStyle = TextStyle(
                            fontFamily = resolveJournalFont(uiState.newChecklistTitle, isRtl),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = JournalInk
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.confirmCreateChecklist { newId ->
                                onOpenChecklist(newId)
                            }
                        })
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmCreateChecklist { newId ->
                            onOpenChecklist(newId)
                        }
                    }
                ) {
                    Text(
                        text = if (isRtl) "إنشاء" else "Créer",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorEmerald
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCreateDialog() }) {
                    Text(
                        text = if (isRtl) "إلغاء" else "Annuler",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 15.sp,
                        color = JournalMutedInk
                    )
                }
            }
        )
    }

    // Delete Confirmation Dialog
    val toDelete = uiState.checklistToDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            containerColor = JournalPaper,
            title = {
                Text(
                    text = if (isRtl) "حذف القائمة ؟" else "Supprimer la checklist ?",
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = JournalInk
                )
            },
            text = {
                Text(
                    text = if (isRtl) {
                        "هل أنت متأكد من رغبتك في حذف « ${toDelete.checklist.title} » وجميع عناصرها نهائياً؟"
                    } else {
                        "Voulez-vous vraiment supprimer « ${toDelete.checklist.title} » et tous ses éléments ?"
                    },
                    fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                    fontSize = 14.5.sp,
                    color = JournalInk
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteChecklist() }) {
                    Text(
                        text = if (isRtl) "حذف" else "Supprimer",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorCoral
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text(
                        text = if (isRtl) "إلغاء" else "Annuler",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 15.sp,
                        color = JournalMutedInk
                    )
                }
            }
        )
    }
}
