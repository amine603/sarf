package com.cash.guide.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cash.guide.domain.ai.CalculationAiEntry
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper

@Composable
fun AiChecklistReviewDialog(
    originalSpeech: String,
    initialItems: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val items = remember { mutableStateListOf<String>().apply { addAll(initialItems) } }
    var newItemText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = JournalPaper),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مراجعة السلعة المستخرجة 📋",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = JournalInk
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إلغاء", tint = JournalMutedInk)
                    }
                }

                // Quote of what was heard
                if (originalSpeech.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF48FB1).copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF48FB1).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💬 \"$originalSpeech\"",
                            fontSize = 12.sp,
                            color = JournalInk,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تقدر تمسح 🗑️ أو تصحح أي عنصر قبل ما تأكد:",
                    fontSize = 12.5.sp,
                    color = JournalMutedInk
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Items list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE0D7CD), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B7A4B),
                                fontSize = 13.sp,
                                modifier = Modifier.width(22.dp)
                            )
                            BasicTextField(
                                value = item,
                                onValueChange = { newText -> items[index] = newText },
                                textStyle = TextStyle(
                                    color = JournalInk,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(Color(0xFF1B7A4B)),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { items.removeAt(index) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    // Add manual item row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = newItemText,
                                onValueChange = { newItemText = it },
                                textStyle = TextStyle(color = JournalInk, fontSize = 13.5.sp),
                                cursorBrush = SolidColor(Color(0xFF1B7A4B)),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFD7CCC8), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        if (newItemText.isBlank()) {
                                            Text(
                                                text = "+ زيد عنصر آخر يدوياً...",
                                                color = JournalMutedInk,
                                                fontSize = 12.5.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (newItemText.isNotBlank()) Color(0xFF1B7A4B) else Color.LightGray,
                                modifier = Modifier.clickable(enabled = newItemText.isNotBlank()) {
                                    items.add(newItemText.trim())
                                    newItemText = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "إضافة",
                                    tint = Color.White,
                                    modifier = Modifier.padding(6.dp).size(18.dp)
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
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDismiss() }
                    ) {
                        Text(
                            text = "إلغاء",
                            color = JournalInk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (items.isNotEmpty()) Color(0xFF1B7A4B) else Color.Gray,
                        modifier = Modifier
                            .weight(2f)
                            .clickable(enabled = items.isNotEmpty()) {
                                onConfirm(items.filter { it.isNotBlank() })
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
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
                                text = "تأكيد وإضافة (${items.size}) ✅",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = JournalPaper),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مراجعة الحسابات المستخرجة 💰",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = JournalInk
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إلغاء", tint = JournalMutedInk)
                    }
                }

                // Quote
                if (originalSpeech.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF48FB1).copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF48FB1).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💬 \"$originalSpeech\"",
                            fontSize = 12.sp,
                            color = JournalInk,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "الأثمنة اختيارية! تقدر تكتب الثمن دابا أو تخليه فارغ:",
                    fontSize = 12.sp,
                    color = JournalMutedInk
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Entries list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    itemsIndexed(entries) { index, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFE0D7CD), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (entry.existingRowId != null) {
                                Surface(
                                    color = Color(0xFFE3F2FD),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = "تحديث 🔄",
                                        color = Color(0xFF1565C0),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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
                                    color = JournalInk,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(Color(0xFF1B7A4B)),
                                modifier = Modifier.weight(1.5f)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Amount field
                            Box(
                                modifier = Modifier
                                    .width(78.dp)
                                    .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFD7CCC8), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (entry.amountStr.isBlank()) {
                                    Text(
                                        text = "0 DH",
                                        color = Color.LightGray,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                BasicTextField(
                                    value = entry.amountStr,
                                    onValueChange = { newAmt ->
                                        entries[index] = entry.copy(amountStr = newAmt.filter { it.isDigit() || it == '.' })
                                    },
                                    textStyle = TextStyle(
                                        color = Color(0xFF1B7A4B),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.End
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    cursorBrush = SolidColor(Color(0xFF1B7A4B)),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "DH", fontSize = 11.sp, color = JournalMutedInk)

                            IconButton(
                                onClick = { entries.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Add row manually
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = newLabel,
                                onValueChange = { newLabel = it },
                                textStyle = TextStyle(color = JournalInk, fontSize = 13.sp),
                                cursorBrush = SolidColor(Color(0xFF1B7A4B)),
                                decorationBox = { inner ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .border(1.dp, Color(0xFFD7CCC8), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 5.dp)
                                    ) {
                                        if (newLabel.isBlank()) Text("+ بند إضافي...", color = JournalMutedInk, fontSize = 12.sp)
                                        inner()
                                    }
                                },
                                modifier = Modifier.weight(1.5f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = newAmount,
                                onValueChange = { newAmount = it.filter { c -> c.isDigit() || c == '.' } },
                                textStyle = TextStyle(color = Color(0xFF1B7A4B), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                cursorBrush = SolidColor(Color(0xFF1B7A4B)),
                                decorationBox = { inner ->
                                    Box(
                                        modifier = Modifier
                                            .width(65.dp)
                                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .border(1.dp, Color(0xFFD7CCC8), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 5.dp)
                                    ) {
                                        if (newAmount.isBlank()) Text("الثمن", color = JournalMutedInk, fontSize = 12.sp, textAlign = TextAlign.End)
                                        inner()
                                    }
                                },
                                modifier = Modifier.width(65.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (newLabel.isNotBlank()) Color(0xFF1B7A4B) else Color.LightGray,
                                modifier = Modifier.clickable(enabled = newLabel.isNotBlank()) {
                                    entries.add(EditableAiCalcEntry(newLabel.trim(), newAmount.trim()))
                                    newLabel = ""
                                    newAmount = ""
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "إضافة",
                                    tint = Color.White,
                                    modifier = Modifier.padding(5.dp).size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Total Bar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF9C4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المجموع المحسوب:",
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            fontSize = 13.5.sp
                        )
                        val totalStr = if (totalDh % 1.0 == 0.0) "${totalDh.toLong()} DH" else String.format(java.util.Locale.US, "%.2f DH", totalDh)
                        Text(
                            text = totalStr,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1B7A4B),
                            fontSize = 15.sp
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
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDismiss() }
                    ) {
                        Text(
                            text = "إلغاء",
                            color = JournalInk,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (entries.isNotEmpty()) Color(0xFF1B7A4B) else Color.Gray,
                        modifier = Modifier
                            .weight(2f)
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
                            modifier = Modifier.padding(vertical = 12.dp),
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
                                text = "تأكيد وإضافة (${entries.size}) ✅",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
