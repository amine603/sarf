package com.cash.guide.ui.notebook

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.domain.BackspaceRepeatController

/**
 * Data structure for an emoji category.
 */
data class JournalEmojiCategory(
    val id: String,
    val icon: String,
    val nameFr: String,
    val nameAr: String,
    val emojis: List<String>
)

/**
 * Curated Moroccan daily business, trade, and bookkeeping emojis pack.
 */
object JournalEmojiData {
    val quickEmojis: List<String> = listOf(
        "🛒", "🏗️", "☕", "🚗", "🏠", "🥩", "👥", "💰", "🧱", "🔨", "🥖", "💡", "😊"
    )

    val categories: List<JournalEmojiCategory> = listOf(
        JournalEmojiCategory(
            id = "commerce",
            icon = "🛒",
            nameFr = "Commerce & Marché",
            nameAr = "تجارة وسلعة",
            emojis = listOf(
                "🛒", "🛍️", "🏪", "🏬", "📦", "🏷️", "🥖", "🥐", "🍞", "🥩",
                "🍗", "🐟", "🥚", "🥛", "🧀", "🧈", "🍯", "🥫", "🧂", "🍅",
                "🥔", "🧅", "🥕", "🧄", "🍎", "🍌", "🍊", "🍋", "🍉", "🍇",
                "🍓", "🥑", "🍫", "🍬", "🫒", "🥒", "🥬", "🌽", "🍄", "🥜"
            )
        ),
        JournalEmojiCategory(
            id = "chantier",
            icon = "🏗️",
            nameFr = "Chantier & Bâtiment",
            nameAr = "بناء وحرف",
            emojis = listOf(
                "🏗️", "🧱", "🔨", "🔧", "🪛", "🪜", "🪚", "🔩", "⚙️", "🧰",
                "📐", "📏", "🎨", "🖌️", "🪵", "🔌", "💡", "🔦", "🚜", "🚛",
                "🚚", "🛢️", "🦺", "🥾", "🧹", "🪣", "✂️", "⛏️", "⛓️", "🚪",
                "🪟", "🧲", "🪓"
            )
        ),
        JournalEmojiCategory(
            id = "resto",
            icon = "☕",
            nameFr = "Café & Resto",
            nameAr = "مقهى ومطعم",
            emojis = listOf(
                "☕", "🫖", "🍵", "🥤", "🧃", "🍼", "🍽️", "🍴", "🥄", "🥪",
                "🍔", "🍕", "🌮", "🍳", "🍲", "🥗", "🥣", "🥘", "🍝", "🍨",
                "🍰", "🧁", "🍪", "🍩", "🧇", "🥞"
            )
        ),
        JournalEmojiCategory(
            id = "transport",
            icon = "🚗",
            nameFr = "Transport & Véhicules",
            nameAr = "تنقل وأسفار",
            emojis = listOf(
                "🚗", "🚕", "🚖", "🚌", "🚎", "🚐", "🚲", "🛵", "🏍️", "⛽",
                "🅿️", "🎫", "🛣️", "🚦", "🚨", "✈️", "🧳", "🚢", "🚆", "🚇",
                "🏎️", "🚂"
            )
        ),
        JournalEmojiCategory(
            id = "maison",
            icon = "🏠",
            nameFr = "Maison & Charges",
            nameAr = "منزل وفواتير",
            emojis = listOf(
                "🏠", "🏢", "🏡", "🔑", "🗝️", "🚿", "🛁", "💧", "⚡", "📶",
                "📱", "💻", "🖥️", "🧾", "🛋️", "🛏️", "🧼", "🧴", "🧹", "🧺",
                "📺", "📻", "❄️", "☀️", "🌡️", "🪴", "🪑"
            )
        ),
        JournalEmojiCategory(
            id = "finance",
            icon = "💰",
            nameFr = "Flous & Monnaie",
            nameAr = "مالية وفلوس",
            emojis = listOf(
                "💰", "💵", "🪙", "💳", "🏦", "🧾", "📈", "📉", "📊", "💼",
                "📂", "📁", "📝", "✏️", "✒️", "🖊️", "🔒", "🔓", "🏷️", "💎",
                "⚖️", "📌", "📍"
            )
        ),
        JournalEmojiCategory(
            id = "equipe",
            icon = "👥",
            nameFr = "Équipe & Travail",
            nameAr = "عمال ويوميات",
            emojis = listOf(
                "👥", "👤", "👨‍💼", "👩‍💼", "👷", "👷‍♀️", "👨‍🌾", "👩‍🍳", "👨‍🔧", "🤝",
                "🤲", "💊", "🩺", "🩹", "👕", "👖", "👟", "👞", "👓", "🎒",
                "🎁", "💍", "🕌", "🌙", "⭐"
            )
        ),
        JournalEmojiCategory(
            id = "smilies",
            icon = "😊",
            nameFr = "Symboles & Smilies",
            nameAr = "رموز وعلامات",
            emojis = listOf(
                "😊", "😀", "😃", "😄", "😉", "😍", "😎", "🤔", "👍", "👎",
                "👌", "✌️", "👏", "🙌", "🤝", "💯", "✅", "✔️", "❌", "⚠️",
                "❗", "❓", "🎯", "📌", "📍", "⭐", "🔥", "✨"
            )
        )
    )
}

/**
 * Compact Quick Emoji Bar displayed in the keyboard header strip.
 */
@Composable
fun JournalQuickEmojiBar(
    onSelectEmoji: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        JournalEmojiData.quickEmojis.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button) { onSelectEmoji(emoji) }
                    .semantics { testTag = "tag_quick_emoji_$emoji" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Full Emoji Keyboard Panel rendered inside the keyboard dock area.
 * Features categorized tabs, neat compact emoji grid, and a bottom control bar (ABC, 123, space, backspace, OK).
 */
@Composable
fun JournalEmojiKeyboardPanel(
    onInsertEmoji: (String) -> Unit,
    onSwitchToAlphabet: () -> Unit,
    onSwitchToNumericMode: () -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
    backspaceController: BackspaceRepeatController,
    spaceLabel: String = "espace",
    modifier: Modifier = Modifier
) {
    var selectedCategoryId by remember { mutableStateOf(JournalEmojiData.categories.first().id) }
    val currentCategory = remember(selectedCategoryId) {
        JournalEmojiData.categories.firstOrNull { it.id == selectedCategoryId }
            ?: JournalEmojiData.categories.first()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .semantics { testTag = "tag_emoji_panel" }
    ) {
        // --- 1. Category Tabs Row ---
        val categoryScrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .horizontalScroll(categoryScrollState)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            JournalEmojiData.categories.forEach { category ->
                val isSelected = category.id == selectedCategoryId
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(role = Role.Tab) { selectedCategoryId = category.id }
                        .semantics { testTag = "tag_emoji_cat_${category.id}" }
                        .drawBehind {
                            if (isSelected) {
                                drawRect(HighlighterPink.copy(alpha = 0.35f))
                                val strokeW = 1.8.dp.toPx()
                                drawLine(
                                    color = JournalInk,
                                    start = Offset(4.dp.toPx(), size.height),
                                    end = Offset(size.width - 4.dp.toPx(), size.height),
                                    strokeWidth = strokeW,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.icon,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Delicate notebook divider
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 4.dp),
            thickness = 0.6.dp,
            color = JournalRule.copy(alpha = 0.40f)
        )

        // --- 2. Compact Emoji Grid (Small size: "o sghar ok") ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 34.dp),
                contentPadding = PaddingValues(2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentCategory.emojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(role = Role.Button) { onInsertEmoji(emoji) }
                            .semantics { testTag = "tag_emoji_item_$emoji" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 19.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Delicate notebook divider before bottom bar
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 4.dp),
            thickness = 0.6.dp,
            color = JournalRule.copy(alpha = 0.40f)
        )

        // --- 3. Bottom Utility Row (ABC, 123, Espace, ⌫, OK ✓) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ABC button (switch back to alphabet)
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .clickable(role = Role.Button, onClick = onSwitchToAlphabet)
                    .semantics { testTag = "tag_emoji_to_abc" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ABC",
                    style = TextStyle(
                        fontFamily = JournalHandFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk
                    )
                )
            }

            // 123 button (switch to numbers)
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clickable(role = Role.Button, onClick = onSwitchToNumericMode)
                    .semantics { testTag = "tag_emoji_to_num" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "123",
                    style = TextStyle(
                        fontFamily = JournalHandFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalInk
                    )
                )
            }

            // Space button
            Box(
                modifier = Modifier
                    .weight(3.4f)
                    .fillMaxHeight()
                    .clickable(role = Role.Button) { onInsertEmoji(" ") },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = spaceLabel,
                    style = TextStyle(
                        fontFamily = JournalHandFamily,
                        fontSize = 14.sp,
                        color = JournalMutedInk
                    )
                )
            }

            // Backspace button (with repeat controller)
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .pointerInput(backspaceController) {
                        detectTapGestures(
                            onPress = {
                                backspaceController.onPointerDown()
                                val released = tryAwaitRelease()
                                if (released) {
                                    backspaceController.onPointerUp()
                                } else {
                                    backspaceController.cancel()
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(20.dp, 15.dp)) {
                    val strokeW = 1.15.dp.toPx()
                    val ink = JournalInk.copy(alpha = 0.9f)
                    val w = size.width
                    val h = size.height

                    val p = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.32f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h)
                        lineTo(w * 0.32f, h)
                        lineTo(0f, h / 2f)
                        close()
                    }
                    drawPath(p, color = ink, style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW))

                    val cx = w * 0.64f
                    val cy = h / 2f
                    val d = 2.8.dp.toPx()
                    drawLine(ink, Offset(cx - d, cy - d), Offset(cx + d, cy + d), strokeW, StrokeCap.Round)
                    drawLine(ink, Offset(cx + d, cy - d), Offset(cx - d, cy + d), strokeW, StrokeCap.Round)
                }
            }

            // OK Confirm button
            Box(
                modifier = Modifier
                    .weight(1.6f)
                    .fillMaxHeight()
                    .clickable(role = Role.Button, onClick = onConfirm)
                    .semantics { testTag = "tag_emoji_confirm" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OK ✓",
                    style = TextStyle(
                        fontFamily = JournalHandFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = JournalActionConfirm
                    )
                )
            }
        }
    }
}
