package com.cash.guide.feature.note

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
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
import androidx.core.content.ContextCompat
import com.cash.guide.domain.NoteShareHelper
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.JournalActionDelete
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.JournalRuleSpacing
import com.cash.guide.ui.notebook.JournalRuledDocument
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.resolveJournalFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteEditorScreen(
    viewModel: NoteViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    val dateFormatter = remember { SimpleDateFormat("EEEE d MMMM yyyy • HH:mm", Locale.getDefault()) }
    val formattedDate = remember(uiState.createdAtEpochMs) {
        dateFormatter.format(Date(uiState.createdAtEpochMs)).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    val availableTags = remember {
        listOf("DEFAULT", "YELLOW", "PINK", "BLUE", "GREEN", "PURPLE")
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
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

                        // Centered Title Pill
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
                                text = uiState.title.ifBlank { if (isRtl) "ملاحظة" else "Note" },
                                fontFamily = resolveJournalFont(uiState.title, isRtl),
                                fontSize = if (isRtl) 16.5.sp else 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalWritingInk,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                        }

                        // Action Icons: Pin + Share + Trash
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Pin Toggle
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable(role = Role.Button) { viewModel.togglePin() },
                                contentAlignment = Alignment.Center
                            ) {
                                HisabiSketchIcon(
                                    symbol = HisabiSymbol.Pin,
                                    contentDescription = "Épingler",
                                    tint = if (uiState.isPinned) Color(0xFFE11D48) else JournalInk.copy(alpha = 0.60f),
                                    size = 18.dp
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

                    // Color palette chips row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isRtl) "اللون:" else "Couleur :",
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 12.5.sp,
                            color = JournalMutedInk
                        )

                        availableTags.forEach { tag ->
                            val isSelected = uiState.colorTag.equals(tag, ignoreCase = true)
                            val colors = getNoteCardColors(tag)
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(colors.background)
                                    .border(
                                        BorderStroke(
                                            if (isSelected) 2.dp else 1.dp,
                                            if (isSelected) JournalWritingInk else colors.outline
                                        ),
                                        CircleShape
                                    )
                                    .clickable(role = Role.Button) { viewModel.setColorTag(tag) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(JournalWritingInk)
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

            // Ruled Note Body
            JournalRuledDocument(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                clearFocusOnTap = false
            ) {
                // Rule 1: Date stamp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = formattedDate,
                        fontFamily = PatrickHandFamily,
                        fontSize = 13.sp,
                        color = JournalMutedInk.copy(alpha = 0.75f),
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.journalBaselineOnRule()
                    )
                }

                // Rule 2: Title Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(JournalRuleSpacing)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    BasicTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        textStyle = TextStyle(
                            fontFamily = resolveJournalFont(uiState.title, isRtl),
                            fontSize = if (isRtl) 19.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalWritingInk,
                            platformStyle = NoFontPadding
                        ),
                        cursorBrush = SolidColor(JournalWritingInk),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .journalBaselineOnRule(opticalOffsetFromBottom = 2.dp),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (uiState.title.isEmpty()) {
                                    Text(
                                        text = if (isRtl) "عنوان الملاحظة..." else "Titre de la note...",
                                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                        fontSize = if (isRtl) 19.sp else 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = JournalMutedInk.copy(alpha = 0.45f),
                                        style = TextStyle(platformStyle = NoFontPadding)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                // Rule 3: Ruled Spacer
                Spacer(modifier = Modifier.height(JournalRuleSpacing))

                // Multiline content flowing on rules
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    BasicTextField(
                        value = uiState.content,
                        onValueChange = { viewModel.updateContent(it) },
                        textStyle = TextStyle(
                            fontFamily = resolveJournalFont(uiState.content, isRtl),
                            fontSize = if (isRtl) 15.5.sp else 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = JournalInk,
                            lineHeight = 29.sp,
                            platformStyle = NoFontPadding
                        ),
                        cursorBrush = SolidColor(JournalWritingInk),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (uiState.content.isEmpty()) {
                                    Text(
                                        text = if (isRtl) {
                                            "اكتب أفكارك، خواطرك، أو أي حاجة بغيتي تفكرها هنا... ✍️\nتقدر تضغط على زر المايك لتحت وتهضر مباشرة!"
                                        } else {
                                            "Écrivez vos pensées, idées ou mémos ici... ✍️\nVous pouvez aussi appuyer sur le micro pour dicter !"
                                        },
                                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                                        fontSize = if (isRtl) 15.sp else 15.5.sp,
                                        color = JournalMutedInk.copy(alpha = 0.45f),
                                        lineHeight = 29.sp,
                                        style = TextStyle(platformStyle = NoFontPadding)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(JournalRuleSpacing * 6))
            }

            // Bottom Floating Bar: Voice Dictation Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding(),
                color = JournalPaper,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Dictation indicator
                        Text(
                            text = if (uiState.isListening) {
                                if (isRtl) "🔴 كنسمع ليك دابا، تفضل هضر..." else "🔴 Écoute en cours, parlez..."
                            } else {
                                if (isRtl) "🎙️ اضغط على المايك للإملاء الصوتي" else "🎙️ Dictée vocale rapide"
                            },
                            fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                            fontSize = 13.5.sp,
                            fontWeight = if (uiState.isListening) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.isListening) Color(0xFFE11D48) else JournalMutedInk
                        )

                        // Mic Action Button
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isListening) Color(0xFFFFE4E6) else HighlighterPink.copy(alpha = 0.40f)
                                )
                                .border(
                                    BorderStroke(
                                        1.5.dp,
                                        if (uiState.isListening) Color(0xFFE11D48) else Color(0xFFF48FB1)
                                    ),
                                    CircleShape
                                )
                                .clickable(role = Role.Button) {
                                    if (uiState.isListening) {
                                        viewModel.stopListening()
                                    } else {
                                        val hasPerm = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasPerm) {
                                            viewModel.startListening()
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.isListening) "⏹️" else "🎙️",
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = if (isRtl) "مسح الملاحظة؟" else "Supprimer la note ?",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalWritingInk
                    )
                },
                text = {
                    Text(
                        text = if (isRtl) "واش متأكد باغي تمسح هاد الملاحظة نهائياً؟" else "Voulez-vous vraiment supprimer définitivement cette note ?",
                        fontFamily = if (isRtl) TajawalFamily else PatrickHandFamily,
                        fontSize = 14.5.sp,
                        color = JournalMutedInk
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
                            text = if (isRtl) "مسح" else "Supprimer",
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
