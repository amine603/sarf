package com.cash.guide.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cash.guide.domain.ai.CalculationAiEntry
import com.cash.guide.ui.notebook.ColorCoral
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.JournalActionConfirm
import com.cash.guide.ui.notebook.JournalActionDelete
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.NumberBoldVisualTransformation
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.highlightNumbersInText
import com.cash.guide.ui.notebook.isArabicScript
import com.cash.guide.ui.notebook.journalDashedBorder
import com.cash.guide.ui.notebook.resolveJournalFont

@Composable
fun AiChecklistReviewDialog(
    originalSpeech: String,
    initialItems: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val items = remember { mutableStateListOf<String>().apply { addAll(initialItems) } }
    var newItemText by remember { mutableStateOf("") }

    val currentLocale = androidx.compose.ui.platform.LocalConfiguration.current.locales.get(0)
    val appLang = currentLocale?.language ?: "ar"
    val isFrench = appLang == "fr"
    val isEnglish = appLang == "en"
    val isRtl = !isFrench && !isEnglish
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    val dialogTitle = when {
        isFrench -> "Vérification des éléments 📋"
        isEnglish -> "Review extracted items 📋"
        else -> "مراجعة السلعة المستخرجة 📋"
    }
    val dialogSub = when {
        isFrench -> "Vous pouvez supprimer ou modifier avant de confirmer :"
        isEnglish -> "You can delete or edit items before confirming:"
        else -> "تقدر تمسح أو تصحح أي عنصر قبل ما تأكد:"
    }
    val addPlaceholder = when {
        isFrench -> "+ Ajouter un autre élément..."
        isEnglish -> "+ Add another item..."
        else -> "+ زيد عنصر آخر يدوياً..."
    }
    val cancelBtn = when {
        isFrench -> "Annuler"
        isEnglish -> "Cancel"
        else -> "إلغاء"
    }
    val confirmBtn = when {
        isFrench -> "Confirmer et ajouter (${items.size}) ✅"
        isEnglish -> "Confirm and add (${items.size}) ✅"
        else -> "تأكيد وإضافة (${items.size}) ✅"
    }

    val rowDotColors = remember {
        listOf(
            Color(0xFF5B9EC9), // Soft Sky Blue
            Color(0xFFE27B97), // Soft Rose Pink
            Color(0xFF7FA85B), // Soft Sage Green
            Color(0xFFE0B038), // Soft Warm Amber
            Color(0xFF9878C8), // Soft Lavender
            Color(0xFFE28862)  // Soft Peach Coral
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .wrapContentHeight()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp))
                    .border(
                        BorderStroke(1.2.dp, JournalRule.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = JournalPaper,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // 1. Subtle tactile paper grain / flecks
                            val dotColor = JournalInk.copy(alpha = 0.025f)
                            var px = 16f
                            while (px < size.width) {
                                var py = 18f
                                while (py < size.height) {
                                    drawCircle(
                                        color = dotColor,
                                        radius = 0.9f,
                                        center = Offset(px, py)
                                    )
                                    py += 56f
                                }
                                px += 44f
                            }

                            // 2. Notebook margin guide line
                            val marginX = if (isRtl) size.width - 24.dp.toPx() else 24.dp.toPx()
                            drawLine(
                                color = ColorCoral.copy(alpha = 0.22f),
                                start = Offset(marginX, 0f),
                                end = Offset(marginX, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dialogTitle,
                            fontFamily = resolveJournalFont(dialogTitle, isRtl),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = JournalInk
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = cancelBtn,
                                tint = JournalMutedInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Quote of what was heard
                    if (originalSpeech.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HighlighterYellow.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, JournalRule.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val isQuoteArabic = isArabicScript(originalSpeech)
                            Text(
                                text = highlightNumbersInText("💬 \"$originalSpeech\"", JournalInk),
                                fontFamily = resolveJournalFont(originalSpeech, isQuoteArabic),
                                fontSize = 13.sp,
                                color = JournalInk,
                                lineHeight = 18.sp,
                                textAlign = if (isQuoteArabic) TextAlign.Right else TextAlign.Left,
                                style = TextStyle(textDirection = if (isQuoteArabic) TextDirection.Rtl else TextDirection.Ltr),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dialogSub,
                        fontFamily = resolveJournalFont(dialogSub, isRtl),
                        fontSize = 12.5.sp,
                        color = JournalMutedInk,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Items list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp)
                    ) {
                        itemsIndexed(items) { index, item ->
                            val isItemArabic = isArabicScript(item)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawBehind {
                                        drawLine(
                                            color = JournalRule.copy(alpha = 0.45f),
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 0.8.dp.toPx()
                                        )
                                    }
                                    .padding(vertical = 6.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Watercolor bullet dot
                                Canvas(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(7.dp)
                                ) {
                                    drawCircle(color = rowDotColors[index % rowDotColors.size])
                                }

                                Text(
                                    text = "${index + 1}.",
                                    fontFamily = PatrickHandFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = JournalActionConfirm,
                                    fontSize = 14.sp,
                                    modifier = Modifier.width(22.dp)
                                )

                                BasicTextField(
                                    value = item,
                                    onValueChange = { newText -> items[index] = newText },
                                    textStyle = TextStyle(
                                        fontFamily = resolveJournalFont(item, isItemArabic),
                                        color = JournalInk,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal,
                                        textAlign = if (isItemArabic) TextAlign.Right else TextAlign.Left,
                                        textDirection = if (isItemArabic) TextDirection.Rtl else TextDirection.Ltr
                                    ),
                                    visualTransformation = NumberBoldVisualTransformation,
                                    cursorBrush = SolidColor(JournalActionConfirm),
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = { items.removeAt(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = cancelBtn,
                                        tint = JournalActionDelete,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Add manual item row
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = newItemText,
                                    onValueChange = { newItemText = it },
                                    textStyle = TextStyle(
                                        fontFamily = resolveJournalFont(newItemText, isRtl),
                                        color = JournalInk,
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(JournalActionConfirm),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .journalDashedBorder(
                                                    color = JournalRule.copy(alpha = 0.6f),
                                                    cornerRadius = 6.dp
                                                )
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            if (newItemText.isBlank()) {
                                                Text(
                                                    text = addPlaceholder,
                                                    fontFamily = resolveJournalFont(addPlaceholder, isRtl),
                                                    color = JournalMutedInk,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (newItemText.isNotBlank()) JournalActionConfirm else JournalRule.copy(alpha = 0.45f),
                                    modifier = Modifier.clickable(enabled = newItemText.isNotBlank()) {
                                        items.add(newItemText.trim())
                                        newItemText = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp).size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.2.dp, JournalRule.copy(alpha = 0.75f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onDismiss() }
                        ) {
                            Text(
                                text = cancelBtn,
                                fontFamily = resolveJournalFont(cancelBtn, isRtl),
                                color = JournalMutedInk,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 11.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (items.isNotEmpty()) JournalActionConfirm else JournalRule.copy(alpha = 0.45f),
                            modifier = Modifier
                                .weight(1.8f)
                                .clickable(enabled = items.isNotEmpty()) {
                                    onConfirm(items.filter { it.isNotBlank() })
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = confirmBtn,
                                    fontFamily = resolveJournalFont(confirmBtn, isRtl),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class EditableAiCalcEntry(
    var label: String,
    var amountStr: String,
    val existingRowId: String? = null
)

@Composable
fun AiCalculationReviewDialog(
    originalSpeech: String,
    initialEntries: List<CalculationAiEntry>,
    onDismiss: () -> Unit,
    onConfirm: (List<CalculationAiEntry>) -> Unit
) {
    val entries = remember {
        mutableStateListOf<EditableAiCalcEntry>().apply {
            addAll(initialEntries.map {
                val str = if (it.amount <= 0.0) "" else if (it.amount % 1.0 == 0.0) it.amount.toLong().toString() else String.format(java.util.Locale.US, "%.2f", it.amount)
                EditableAiCalcEntry(it.label, str, it.existingRowId)
            })
        }
    }

    var newLabel by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }

    val totalDh = entries.sumOf { it.amountStr.toDoubleOrNull() ?: 0.0 }

    val currentLocale = androidx.compose.ui.platform.LocalConfiguration.current.locales.get(0)
    val appLang = currentLocale?.language ?: "ar"
    val isFrench = appLang == "fr"
    val isEnglish = appLang == "en"
    val isRtl = !isFrench && !isEnglish
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    val dialogTitle = when {
        isFrench -> "Vérification des calculs 💰"
        isEnglish -> "Review extracted rows 💰"
        else -> "مراجعة الحسابات المستخرجة 💰"
    }
    val dialogSub = when {
        isFrench -> "Les montants sont optionnels ! Vous pouvez les modifier ou laisser vide :"
        isEnglish -> "Amounts are optional! You can edit them now or leave blank:"
        else -> "الأثمنة اختيارية! تقدر تكتب الثمن دابا أو تخليه فارغ:"
    }
    val updateBadge = when {
        isFrench -> "Mise à jour 🔄"
        isEnglish -> "Update 🔄"
        else -> "تحديث 🔄"
    }
    val addRowPlaceholder = when {
        isFrench -> "+ Autre ligne..."
        isEnglish -> "+ Add row..."
        else -> "+ بند إضافي..."
    }
    val pricePlaceholder = when {
        isFrench -> "Prix"
        isEnglish -> "Price"
        else -> "الثمن"
    }
    val totalLabel = when {
        isFrench -> "Total calculé :"
        isEnglish -> "Calculated total:"
        else -> "المجموع المحسوب:"
    }
    val cancelBtn = when {
        isFrench -> "Annuler"
        isEnglish -> "Cancel"
        else -> "إلغاء"
    }
    val confirmBtn = when {
        isFrench -> "Confirmer et ajouter (${entries.size}) ✅"
        isEnglish -> "Confirm and add (${entries.size}) ✅"
        else -> "تأكيد وإضافة (${entries.size}) ✅"
    }

    val rowDotColors = remember {
        listOf(
            Color(0xFF5B9EC9), // Soft Sky Blue
            Color(0xFFE27B97), // Soft Rose Pink
            Color(0xFF7FA85B), // Soft Sage Green
            Color(0xFFE0B038), // Soft Warm Amber
            Color(0xFF9878C8), // Soft Lavender
            Color(0xFFE28862)  // Soft Peach Coral
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .wrapContentHeight()
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp))
                    .border(
                        BorderStroke(1.2.dp, JournalRule.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = JournalPaper,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // 1. Subtle tactile paper grain / flecks
                            val dotColor = JournalInk.copy(alpha = 0.025f)
                            var px = 16f
                            while (px < size.width) {
                                var py = 18f
                                while (py < size.height) {
                                    drawCircle(
                                        color = dotColor,
                                        radius = 0.9f,
                                        center = Offset(px, py)
                                    )
                                    py += 56f
                                }
                                px += 44f
                            }

                            // 2. Notebook margin guide line (warm coral/red)
                            val marginX = if (isRtl) size.width - 24.dp.toPx() else 24.dp.toPx()
                            drawLine(
                                color = ColorCoral.copy(alpha = 0.22f),
                                start = Offset(marginX, 0f),
                                end = Offset(marginX, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dialogTitle,
                            fontFamily = resolveJournalFont(dialogTitle, isRtl),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = JournalInk
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = cancelBtn,
                                tint = JournalMutedInk,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Quote of what was heard
                    if (originalSpeech.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HighlighterYellow.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, JournalRule.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val isQuoteArabic = isArabicScript(originalSpeech)
                            Text(
                                text = highlightNumbersInText("💬 \"$originalSpeech\"", JournalInk),
                                fontFamily = resolveJournalFont(originalSpeech, isQuoteArabic),
                                fontSize = 13.sp,
                                color = JournalInk,
                                lineHeight = 18.sp,
                                textAlign = if (isQuoteArabic) TextAlign.Right else TextAlign.Left,
                                style = TextStyle(textDirection = if (isQuoteArabic) TextDirection.Rtl else TextDirection.Ltr),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = dialogSub,
                        fontFamily = resolveJournalFont(dialogSub, isRtl),
                        fontSize = 12.5.sp,
                        color = JournalMutedInk,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Entries list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        itemsIndexed(entries) { index, entry ->
                            val isEntryArabic = isArabicScript(entry.label)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawBehind {
                                        // Ruled paper baseline
                                        drawLine(
                                            color = JournalRule.copy(alpha = 0.45f),
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 0.8.dp.toPx()
                                        )
                                    }
                                    .padding(vertical = 5.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Watercolor bullet dot
                                Canvas(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(7.dp)
                                ) {
                                    drawCircle(color = rowDotColors[index % rowDotColors.size])
                                }

                                if (entry.existingRowId != null) {
                                    Surface(
                                        color = HighlighterBlue.copy(alpha = 0.45f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(0.8.dp, Color(0xFF1565C0).copy(alpha = 0.3f)),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = updateBadge,
                                            fontFamily = resolveJournalFont(updateBadge, isRtl),
                                            color = Color(0xFF1565C0),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                // Label
                                BasicTextField(
                                    value = entry.label,
                                    onValueChange = {
                                        entries[index] = entry.copy(label = it)
                                    },
                                    textStyle = TextStyle(
                                        fontFamily = resolveJournalFont(entry.label, isEntryArabic),
                                        color = JournalInk,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal,
                                        textAlign = if (isEntryArabic) TextAlign.Right else TextAlign.Left,
                                        textDirection = if (isEntryArabic) TextDirection.Rtl else TextDirection.Ltr
                                    ),
                                    visualTransformation = NumberBoldVisualTransformation,
                                    cursorBrush = SolidColor(JournalActionConfirm),
                                    modifier = Modifier.weight(1.4f)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                // Amount field
                                Box(
                                    modifier = Modifier
                                        .width(74.dp)
                                        .background(JournalPaper.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                        .border(0.9.dp, JournalRule.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (entry.amountStr.isBlank()) {
                                        Text(
                                            text = "0",
                                            fontFamily = PatrickHandFamily,
                                            color = JournalMutedInk.copy(alpha = 0.4f),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    BasicTextField(
                                        value = entry.amountStr,
                                        onValueChange = { newAmt ->
                                            entries[index] = entry.copy(amountStr = newAmt.filter { it.isDigit() || it == '.' })
                                        },
                                        textStyle = TextStyle(
                                            fontFamily = PatrickHandFamily,
                                            color = JournalActionConfirm,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.End
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        cursorBrush = SolidColor(JournalActionConfirm),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "DH",
                                    fontFamily = PatrickHandFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = JournalMutedInk
                                )

                                Spacer(modifier = Modifier.width(2.dp))

                                IconButton(
                                    onClick = { entries.removeAt(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = cancelBtn,
                                        tint = JournalActionDelete,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Add row manually
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = newLabel,
                                    onValueChange = { newLabel = it },
                                    textStyle = TextStyle(
                                        fontFamily = resolveJournalFont(newLabel, isRtl),
                                        color = JournalInk,
                                        fontSize = 14.sp
                                    ),
                                    cursorBrush = SolidColor(JournalActionConfirm),
                                    decorationBox = { inner ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1.4f)
                                                .journalDashedBorder(
                                                    color = JournalRule.copy(alpha = 0.6f),
                                                    cornerRadius = 6.dp
                                                )
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            if (newLabel.isBlank()) {
                                                Text(
                                                    addRowPlaceholder,
                                                    fontFamily = resolveJournalFont(addRowPlaceholder, isRtl),
                                                    color = JournalMutedInk,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            inner()
                                        }
                                    },
                                    modifier = Modifier.weight(1.4f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BasicTextField(
                                    value = newAmount,
                                    onValueChange = { newAmount = it.filter { c -> c.isDigit() || c == '.' } },
                                    textStyle = TextStyle(
                                        fontFamily = PatrickHandFamily,
                                        color = JournalActionConfirm,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    cursorBrush = SolidColor(JournalActionConfirm),
                                    decorationBox = { inner ->
                                        Box(
                                            modifier = Modifier
                                                .width(65.dp)
                                                .journalDashedBorder(
                                                    color = JournalRule.copy(alpha = 0.6f),
                                                    cornerRadius = 6.dp
                                                )
                                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                        ) {
                                            if (newAmount.isBlank()) {
                                                Text(
                                                    pricePlaceholder,
                                                    fontFamily = resolveJournalFont(pricePlaceholder, isRtl),
                                                    color = JournalMutedInk,
                                                    fontSize = 12.5.sp,
                                                    textAlign = TextAlign.End
                                                )
                                            }
                                            inner()
                                        }
                                    },
                                    modifier = Modifier.width(65.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (newLabel.isNotBlank()) JournalActionConfirm else JournalRule.copy(alpha = 0.45f),
                                    modifier = Modifier.clickable(enabled = newLabel.isNotBlank()) {
                                        entries.add(EditableAiCalcEntry(newLabel.trim(), newAmount.trim()))
                                        newLabel = ""
                                        newAmount = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.padding(6.dp).size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total Bar (warm highlighter summary)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HighlighterYellow.copy(alpha = 0.28f),
                        border = BorderStroke(1.dp, JournalRule.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = totalLabel,
                                fontFamily = resolveJournalFont(totalLabel, isRtl),
                                fontWeight = FontWeight.Bold,
                                color = JournalInk,
                                fontSize = 14.5.sp
                            )
                            val totalStr = if (totalDh % 1.0 == 0.0) "${totalDh.toLong()} DH" else String.format(java.util.Locale.US, "%.2f DH", totalDh)
                            Text(
                                text = totalStr,
                                fontFamily = PatrickHandFamily,
                                fontWeight = FontWeight.Bold,
                                color = JournalActionConfirm,
                                fontSize = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.2.dp, JournalRule.copy(alpha = 0.75f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onDismiss() }
                        ) {
                            Text(
                                text = cancelBtn,
                                fontFamily = resolveJournalFont(cancelBtn, isRtl),
                                color = JournalMutedInk,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 11.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (entries.isNotEmpty()) JournalActionConfirm else JournalRule.copy(alpha = 0.45f),
                            modifier = Modifier
                                .weight(1.8f)
                                .clickable(enabled = entries.isNotEmpty()) {
                                    val result = entries.filter { it.label.isNotBlank() }.map {
                                        CalculationAiEntry(
                                            label = it.label.trim(),
                                            amount = it.amountStr.toDoubleOrNull() ?: 0.0,
                                            existingRowId = it.existingRowId
                                        )
                                    }
                                    onConfirm(result)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = confirmBtn,
                                    fontFamily = resolveJournalFont(confirmBtn, isRtl),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
