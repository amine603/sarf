package com.cash.guide.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.domain.MoneyMath
import com.cash.guide.domain.MoneyPiece
import com.cash.guide.domain.MoneyUnit
import com.cash.guide.ui.theme.Charcoal
import com.cash.guide.ui.theme.CharcoalRaised
import com.cash.guide.ui.theme.Copper
import com.cash.guide.ui.theme.CopperSoft
import com.cash.guide.ui.theme.Hairline
import com.cash.guide.ui.theme.Ink
import com.cash.guide.ui.theme.Ivory
import com.cash.guide.ui.theme.MutedInk
import com.cash.guide.ui.theme.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

data class EntryRow(
    val id: Long,
    val title: String = "",
    val expression: String = ""
)

private val EntryRowListSaver = listSaver<SnapshotStateList<EntryRow>, Any>(
    save = { stateList ->
        stateList.flatMap { listOf<Any>(it.id, it.title, it.expression) }
    },
    restore = { flatList ->
        val list = mutableStateListOf<EntryRow>()
        for (i in flatList.indices step 3) {
            val id = (flatList[i] as? Long) ?: (flatList[i] as? Number)?.toLong() ?: 1L
            val title = (flatList.getOrNull(i + 1) as? String).orEmpty()
            val expression = (flatList.getOrNull(i + 2) as? String).orEmpty()
            list.add(EntryRow(id, title, expression))
        }
        if (list.isEmpty()) list.add(EntryRow(id = 1L))
        list
    }
)

private object BanknoteImageCache {
    private val cache = ConcurrentHashMap<String, ImageBitmap>()

    fun get(path: String): ImageBitmap? = cache[path]

    fun decode(context: Context, path: String): ImageBitmap? {
        val existing = cache[path]
        if (existing != null) return existing
        return runCatching {
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            context.assets.open(path).use { stream ->
                BitmapFactory.decodeStream(stream, null, options)?.asImageBitmap()
            }
        }.getOrNull()?.also { cache[path] = it }
    }
}

@Composable
private fun rememberBanknoteImage(path: String?): ImageBitmap? {
    val context = LocalContext.current
    if (path == null) return null
    val bitmapState = produceState<ImageBitmap?>(initialValue = BanknoteImageCache.get(path), key1 = path) {
        if (value == null) {
            val loaded = withContext(Dispatchers.IO) {
                BanknoteImageCache.decode(context, path)
            }
            value = loaded
        }
    }
    return bitmapState.value
}

@Composable
private fun ChevronIcon(directionUp: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val width = size.width
        val height = size.height
        val path = Path().apply {
            if (directionUp) {
                moveTo(width * 0.15f, height * 0.65f)
                lineTo(width * 0.5f, height * 0.35f)
                lineTo(width * 0.85f, height * 0.65f)
            } else {
                moveTo(width * 0.15f, height * 0.35f)
                lineTo(width * 0.5f, height * 0.65f)
                lineTo(width * 0.85f, height * 0.35f)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun BackspaceIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()
        val path = Path().apply {
            moveTo(w * 0.16f, h * 0.5f)
            lineTo(w * 0.44f, h * 0.18f)
            lineTo(w * 0.88f, h * 0.18f)
            lineTo(w * 0.88f, h * 0.82f)
            lineTo(w * 0.44f, h * 0.82f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Cross lines inside
        drawLine(
            color = color,
            start = Offset(w * 0.54f, h * 0.36f),
            end = Offset(w * 0.78f, h * 0.64f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.78f, h * 0.36f),
            end = Offset(w * 0.54f, h * 0.64f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun MoneyListApp() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val focusManager = LocalFocusManager.current
        val coroutineScope = rememberCoroutineScope()
        val listState = rememberLazyListState()

        var nextId by rememberSaveable { mutableLongStateOf(2L) }
        val rows = rememberSaveable(saver = EntryRowListSaver) {
            mutableStateListOf(EntryRow(id = 1L))
        }
        var selectedUnit by rememberSaveable { mutableStateOf(MoneyUnit.RIAL) }
        var activeRowId by rememberSaveable { mutableLongStateOf(1L) }
        var keyboardExpanded by rememberSaveable { mutableStateOf(true) }
        var showBreakdown by rememberSaveable { mutableStateOf(false) }
        var showMenu by remember { mutableStateOf(false) }
        var showResetConfirmDialog by remember { mutableStateOf(false) }
        var isResultValue by rememberSaveable { mutableStateOf(false) }

        val hasData = rows.any { it.title.isNotBlank() || it.expression.isNotBlank() }

        fun updateRow(id: Long, transform: (EntryRow) -> EntryRow) {
            val index = rows.indexOfFirst { it.id == id }
            if (index >= 0) rows[index] = transform(rows[index])
        }

        fun resetNewList() {
            focusManager.clearFocus()
            rows.clear()
            val initialRow = EntryRow(id = 1L)
            rows.add(initialRow)
            activeRowId = 1L
            nextId = 2L
            keyboardExpanded = true
            isResultValue = false
        }

        fun requestReset() {
            if (hasData) {
                showResetConfirmDialog = true
            } else {
                resetNewList()
            }
        }

        fun clearAllEntries() {
            focusManager.clearFocus()
            rows.indices.forEach { index ->
                rows[index] = rows[index].copy(title = "", expression = "")
            }
            isResultValue = false
        }

        fun addRow() {
            focusManager.clearFocus()
            val row = EntryRow(id = nextId++)
            rows += row
            activeRowId = row.id
            keyboardExpanded = true
            isResultValue = false
            coroutineScope.launch {
                listState.animateScrollToItem(rows.size)
            }
        }

        fun selectUnit(unit: MoneyUnit) {
            if (unit == selectedUnit) return
            rows.indices.forEach { index ->
                val row = rows[index]
                rows[index] = row.copy(
                    expression = MoneyMath.convertExpression(row.expression, selectedUnit, unit)
                )
            }
            selectedUnit = unit
            isResultValue = false
        }

        fun applyKey(key: String) {
            val id = activeRowId.takeIf { active -> rows.any { it.id == active } }
                ?: rows.lastOrNull()?.id
                ?: return

            updateRow(id) { row ->
                val current = row.expression
                val operators = setOf('+', '−', '×', '÷')

                val next: String
                when (key) {
                    "⌫" -> {
                        isResultValue = false
                        next = current.dropLast(1)
                    }
                    "=" -> {
                        val evaluated = MoneyMath.evaluate(current)?.stripTrailingZeros()?.toPlainString()
                        if (evaluated != null) {
                            isResultValue = true
                            next = evaluated
                        } else {
                            next = current
                        }
                    }
                    "." -> {
                        if (isResultValue) {
                            isResultValue = false
                            next = "0."
                        } else {
                            val lastPart = current.split('+', '−', '×', '÷').lastOrNull().orEmpty()
                            next = when {
                                lastPart.contains('.') -> current
                                lastPart.isEmpty() -> current + "0."
                                else -> current + "."
                            }
                        }
                    }
                    "+", "−", "×", "÷" -> {
                        isResultValue = false
                        next = when {
                            current.isBlank() -> current
                            current.last() in operators -> current.dropLast(1) + key
                            else -> current + key
                        }
                    }
                    "00" -> {
                        if (isResultValue) {
                            isResultValue = false
                            next = "0"
                        } else {
                            val lastPart = current.split('+', '−', '×', '÷').lastOrNull().orEmpty()
                            next = when {
                                lastPart.isEmpty() || lastPart == "0" -> current
                                current.length < 23 -> current + "00"
                                else -> current
                            }
                        }
                    }
                    else -> { // Digits 0-9
                        if (isResultValue) {
                            isResultValue = false
                            next = key
                        } else {
                            val lastPart = current.split('+', '−', '×', '÷').lastOrNull().orEmpty()
                            next = when {
                                lastPart == "0" && key == "0" -> current
                                lastPart == "0" && key != "0" -> current.dropLast(1) + key
                                current.length < 24 -> current + key
                                else -> current
                            }
                        }
                    }
                }
                row.copy(expression = next)
            }
        }

        val hasInvalidRows = rows.any { it.expression.isNotBlank() && !MoneyMath.isValidExpression(it.expression) }

        val totalCentimes = if (hasInvalidRows) {
            0L
        } else {
            rows.sumOf { row ->
                MoneyMath.toCentimes(row.expression, selectedUnit) ?: 0L
            }
        }

        Surface(modifier = Modifier.fillMaxSize(), color = Ivory) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                AppHeader(
                    onBackClick = ::requestReset,
                    onMenuClick = { showMenu = true },
                    menuExpanded = showMenu,
                    onDismissMenu = { showMenu = false },
                    onNewList = ::requestReset,
                    onClearAll = ::clearAllEntries
                )

                UnitTabs(selectedUnit = selectedUnit, onSelected = ::selectUnit)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "لائحة جديدة",
                        modifier = Modifier.weight(1f),
                        color = Ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•••",
                        color = MutedInk,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { showMenu = true }
                            .padding(4.dp)
                    )
                }

                EntrySheet(
                    rows = rows,
                    activeRowId = activeRowId,
                    unit = selectedUnit,
                    listState = listState,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    onTitleChange = { id, title -> updateRow(id) { it.copy(title = title) } },
                    onAmountSelected = { id ->
                        focusManager.clearFocus()
                        activeRowId = id
                        keyboardExpanded = true
                        isResultValue = false
                    },
                    onTitleFocused = { keyboardExpanded = false },
                    onRemove = { id ->
                        if (rows.size <= 1) {
                            updateRow(id) { it.copy(title = "", expression = "") }
                        } else {
                            rows.removeAll { it.id == id }
                            if (activeRowId == id) {
                                activeRowId = rows.last().id
                            }
                        }
                    },
                    onAdd = ::addRow
                )

                if (keyboardExpanded) {
                    ExpandedTotalStrip(
                        totalCentimes = totalCentimes,
                        primaryUnit = selectedUnit,
                        hasInvalidRows = hasInvalidRows,
                        onShowBreakdown = {
                            if (!hasInvalidRows && totalCentimes > 0) showBreakdown = true
                        }
                    )
                    CalculatorKeyboard(
                        onCollapse = { keyboardExpanded = false },
                        onKey = ::applyKey
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    CollapsedTotalCard(
                        totalCentimes = totalCentimes,
                        primaryUnit = selectedUnit,
                        hasInvalidRows = hasInvalidRows,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onShowBreakdown = {
                            if (!hasInvalidRows && totalCentimes > 0) showBreakdown = true
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CollapsedKeyboard(onExpand = {
                        focusManager.clearFocus()
                        keyboardExpanded = true
                    })
                }
            }
        }

        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                title = { Text("لائحة جديدة", fontWeight = FontWeight.Bold, color = Ink) },
                text = { Text("واش بغيتي تبدا لائحة جديدة وتمسح الحساب الحالي؟", color = Ink) },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetConfirmDialog = false
                            resetNewList()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Copper)
                    ) {
                        Text("نعم، لائحة جديدة")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmDialog = false }) {
                        Text("إلغاء", color = MutedInk)
                    }
                },
                containerColor = Paper,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (showBreakdown && !hasInvalidRows && totalCentimes > 0) {
            MoneyBreakdownSheet(
                totalCentimes = totalCentimes,
                onDismiss = { showBreakdown = false }
            )
        }
    }
}

@Composable
private fun AppHeader(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    menuExpanded: Boolean,
    onDismissMenu: () -> Unit,
    onNewList: () -> Unit,
    onClearAll: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleAction(text = "←", onClick = onBackClick)

            Text(
                text = "الحساب",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = Ink,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Box {
                CircleAction(text = "•••", onClick = onMenuClick)

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = onDismissMenu,
                    offset = DpOffset(x = 0.dp, y = 6.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("لائحة جديدة", fontWeight = FontWeight.Medium) },
                        onClick = {
                            onDismissMenu()
                            onNewList()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("مسح الكل", color = Color(0xFFC0392B)) },
                        onClick = {
                            onDismissMenu()
                            onClearAll()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleAction(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(38.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = Paper,
        border = BorderStroke(1.dp, Hairline),
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Ink,
                fontSize = if (text == "←") 18.sp else 13.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun UnitTabs(selectedUnit: MoneyUnit, onSelected: (MoneyUnit) -> Unit) {
    val units = listOf(MoneyUnit.RIAL, MoneyUnit.DIRHAM)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEBE6DC))
            .border(1.dp, Hairline, RoundedCornerShape(14.dp))
            .padding(3.dp)
    ) {
        units.forEach { unit ->
            val selected = selectedUnit == unit
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (selected) CopperSoft else Color.Transparent)
                    .clickable { onSelected(unit) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.arabicName,
                    color = Ink,
                    fontSize = 16.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun EntrySheet(
    rows: List<EntryRow>,
    activeRowId: Long,
    unit: MoneyUnit,
    listState: LazyListState,
    modifier: Modifier,
    onTitleChange: (Long, String) -> Unit,
    onAmountSelected: (Long) -> Unit,
    onTitleFocused: () -> Unit,
    onRemove: (Long) -> Unit,
    onAdd: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Paper),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "العنوان (اختياري)",
                        modifier = Modifier.weight(1f),
                        color = MutedInk,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "المبلغ",
                        modifier = Modifier.width(112.dp),
                        color = MutedInk,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }

            items(rows, key = { it.id }) { row ->
                val isRowValid = MoneyMath.isValidExpression(row.expression)
                EntryEditorRow(
                    row = row,
                    canDelete = rows.size > 1 || row.title.isNotBlank() || row.expression.isNotBlank(),
                    isActive = row.id == activeRowId,
                    isValid = isRowValid,
                    onTitleChange = { onTitleChange(row.id, it) },
                    onAmountSelected = { onAmountSelected(row.id) },
                    onTitleFocused = onTitleFocused,
                    onRemove = { onRemove(row.id) }
                )
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable(onClick = onAdd),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CopperSoft)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "+  زيد سطر",
                            color = Copper,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryEditorRow(
    row: EntryRow,
    canDelete: Boolean,
    isActive: Boolean,
    isValid: Boolean,
    onTitleChange: (String) -> Unit,
    onAmountSelected: () -> Unit,
    onTitleFocused: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = row.title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .onFocusChanged { if (it.isFocused) onTitleFocused() },
            placeholder = { Text("مثلاً: نعيمة", color = MutedInk, fontSize = 14.sp) },
            trailingIcon = if (canDelete && row.title.isNotBlank()) {
                {
                    Text(
                        text = "×",
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable(onClick = { onTitleChange("") }),
                        color = MutedInk,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else null,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, color = Ink),
            shape = RoundedCornerShape(11.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Copper,
                unfocusedBorderColor = Hairline,
                focusedContainerColor = Paper,
                unfocusedContainerColor = Paper
            )
        )

        val isInvalidState = !isValid && row.expression.isNotBlank()
        val borderColor = when {
            isInvalidState -> Color(0xFFD9534F)
            isActive -> Copper
            else -> Hairline
        }

        Surface(
            modifier = Modifier
                .width(112.dp)
                .height(50.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAmountSelected
                ),
            color = Paper,
            shape = RoundedCornerShape(11.dp),
            border = BorderStroke(
                width = if (isActive || isInvalidState) 1.5.dp else 1.dp,
                color = borderColor
            )
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = row.expression.ifBlank { "0" },
                                color = when {
                                    isInvalidState -> Color(0xFFD9534F)
                                    row.expression.isBlank() -> MutedInk
                                    else -> Ink
                                },
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isActive && row.expression.isNotBlank()) {
                                Text(
                                    text = " |",
                                    color = if (isInvalidState) Color(0xFFD9534F) else Copper,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isInvalidState) {
                            Text(
                                text = "!",
                                color = Color(0xFFD9534F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandedTotalStrip(
    totalCentimes: Long,
    primaryUnit: MoneyUnit,
    hasInvalidRows: Boolean,
    onShowBreakdown: () -> Unit
) {
    val secondaryUnit = if (primaryUnit == MoneyUnit.DIRHAM) MoneyUnit.RIAL else MoneyUnit.DIRHAM
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = !hasInvalidRows && totalCentimes > 0, onClick = onShowBreakdown)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("المجموع المؤقت ", color = Ink, fontSize = 14.sp)
                if (hasInvalidRows) {
                    Text(
                        text = "غير مكتمل",
                        color = Color(0xFFD9534F),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = MoneyMath.fromCentimes(totalCentimes, primaryUnit),
                        color = Copper,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(" ${primaryUnit.arabicName}", color = Ink, fontSize = 14.sp)
                }
            }
            if (!hasInvalidRows) {
                Text(
                    text = "${MoneyMath.fromCentimes(totalCentimes, secondaryUnit)} ${secondaryUnit.arabicName}",
                    color = MutedInk,
                    fontSize = 12.sp
                )
            } else {
                Text(
                    text = "يرجى إكمال العمليات الحسابية",
                    color = MutedInk,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CollapsedTotalCard(
    totalCentimes: Long,
    primaryUnit: MoneyUnit,
    hasInvalidRows: Boolean,
    modifier: Modifier = Modifier,
    onShowBreakdown: () -> Unit
) {
    val secondaryUnit = if (primaryUnit == MoneyUnit.DIRHAM) MoneyUnit.RIAL else MoneyUnit.DIRHAM
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Paper),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("المجموع", color = MutedInk, fontSize = 13.sp)
                if (hasInvalidRows) {
                    Text(
                        text = "عملية غير مكتملة",
                        color = Color(0xFFD9534F),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = MoneyMath.fromCentimes(totalCentimes, primaryUnit),
                            color = Copper,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(" ${primaryUnit.arabicName}", color = Ink, fontSize = 15.sp)
                    }
                    Text(
                        text = "${MoneyMath.fromCentimes(totalCentimes, secondaryUnit)} ${secondaryUnit.arabicName}",
                        color = MutedInk,
                        fontSize = 13.sp
                    )
                }
            }

            Button(
                onClick = onShowBreakdown,
                enabled = !hasInvalidRows && totalCentimes > 0,
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Copper,
                    disabledContainerColor = Copper.copy(alpha = 0.35f)
                )
            ) {
                Text("حسب المجموع", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun CalculatorKeyboard(onCollapse: () -> Unit, onKey: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(262.dp),
        color = Charcoal,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clickable(onClick = onCollapse),
                contentAlignment = Alignment.Center
            ) {
                ChevronIcon(
                    directionUp = false,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.height(2.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Row 1: 1 | 2 | 3 | +
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf("1", "2", "3", "+").forEach { key ->
                            KeyButton(
                                text = key,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                accent = false,
                                onClick = { onKey(key) }
                            )
                        }
                    }

                    // Row 2: 4 | 5 | 6 | −
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf("4", "5", "6", "−").forEach { key ->
                            KeyButton(
                                text = key,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                accent = false,
                                onClick = { onKey(key) }
                            )
                        }
                    }

                    // Row 3: 7 | 8 | 9 | ×
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf("7", "8", "9", "×").forEach { key ->
                            KeyButton(
                                text = key,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                accent = false,
                                onClick = { onKey(key) }
                            )
                        }
                    }

                    // Row 4: . | 0 | ⌫ | ÷
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf(".", "0", "⌫", "÷").forEach { key ->
                            KeyButton(
                                text = key,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                accent = false,
                                onClick = { onKey(key) }
                            )
                        }
                    }

                    // Row 5: = full-width copper button
                    KeyButton(
                        text = "=",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        accent = true,
                        onClick = { onKey("=") }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    modifier: Modifier,
    accent: Boolean = false,
    onClick: () -> Unit
) {
    val isBackspace = text == "⌫"
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        color = if (accent) Copper else CharcoalRaised,
        shape = RoundedCornerShape(11.dp),
        border = if (accent) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isBackspace) {
                BackspaceIcon(
                    color = Color.White,
                    modifier = Modifier.size(24.dp, 17.dp)
                )
            } else {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = if (accent) 22.sp else 19.sp,
                    fontWeight = if (accent) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun CollapsedKeyboard(onExpand: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand),
        color = Charcoal,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChevronIcon(
                directionUp = true,
                color = Copper,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "لوحة الأرقام",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoneyBreakdownSheet(totalCentimes: Long, onDismiss: () -> Unit) {
    val pieces = remember(totalCentimes) { MoneyMath.breakdown(totalCentimes) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Ivory,
        contentColor = Ink,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 16.dp)
        ) {
            Text("قيمة المجموع", fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${MoneyMath.fromCentimes(totalCentimes, MoneyUnit.DIRHAM)} درهم",
                    color = Copper,
                    fontSize = if (totalCentimes > 10_000_000L) 18.sp else 21.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text("=", color = MutedInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${MoneyMath.fromCentimes(totalCentimes, MoneyUnit.RIAL)} ريال",
                    color = Copper,
                    fontSize = if (totalCentimes > 10_000_000L) 18.sp else 21.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Hairline)
            Spacer(Modifier.height(10.dp))
            Text("طريقة تقسيم الفلوس", color = MutedInk, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pieces) { piece -> MoneyPieceRow(piece) }
            }
        }
    }
}

@Composable
private fun MoneyPieceRow(piece: MoneyPiece) {
    val bitmap = rememberBanknoteImage(piece.denomination.assetPath)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Paper,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Hairline)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = piece.denomination.label,
                    modifier = Modifier
                        .width(104.dp)
                        .height(58.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(104.dp)
                        .height(58.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CopperSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(piece.denomination.label, color = Ink, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = piece.denomination.label,
                modifier = Modifier.weight(1f),
                color = Ink,
                fontSize = 16.sp
            )
            Text(
                text = "× ${piece.count}",
                color = Copper,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
