package com.cash.guide.feature.showcase

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R
import com.cash.guide.ui.notebook.HighlighterBlue
import com.cash.guide.ui.notebook.HighlighterGreen
import com.cash.guide.ui.notebook.HighlighterPink
import com.cash.guide.ui.notebook.HighlighterYellow
import com.cash.guide.ui.notebook.HisabiMetrics
import com.cash.guide.ui.notebook.HisabiSketchIcon
import com.cash.guide.ui.notebook.HisabiSymbol
import com.cash.guide.ui.notebook.JournalDoubleUnderline
import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalWritingInk
import com.cash.guide.ui.notebook.JournalMutedInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalRule
import com.cash.guide.ui.notebook.ManropeFamily
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.NotebookCheckmark
import com.cash.guide.ui.notebook.NotebookCircledNumber
import com.cash.guide.ui.notebook.NotebookCross
import com.cash.guide.ui.notebook.NotebookHeart
import com.cash.guide.ui.notebook.NotebookHighlightedBadge
import com.cash.guide.ui.notebook.NotebookSparkle
import com.cash.guide.ui.notebook.NotebookStickyNote
import com.cash.guide.ui.notebook.NotebookUnderlineWithDot
import com.cash.guide.ui.notebook.NotebookWavyUnderline
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.TajawalFamily
import com.cash.guide.ui.notebook.journalAmountStyle
import com.cash.guide.ui.notebook.journalBaselineOnRule
import com.cash.guide.ui.notebook.journalDashedBorder
import com.cash.guide.ui.notebook.journalHighlighter
import com.cash.guide.ui.notebook.journalPrimaryTotalStyle
import com.cash.guide.ui.notebook.journalSuffixStyle
import com.cash.guide.ui.notebook.journalTitleStyle
import com.cash.guide.ui.notebook.notebookUnderlineWithDot
import com.cash.guide.ui.notebook.notebookWavyUnderline

/**
 * Dedicated Showcase Screen (Style Lab):
 * Demonstrates writing directly on active ruled lines with student/bullet-journal decorative flourishes.
 */
@Composable
fun TestNotebookShowcaseScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val ruleSpacing = HisabiMetrics.Grid // 29.dp matching Homepage reference

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JournalPaper)
    ) {
        // --- Top Bar ---
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
                    .height(56.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    HisabiSketchIcon(
                        symbol = HisabiSymbol.Back,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = JournalInk,
                        size = 20.dp
                    )
                }

                // Title with Watercolor Pink Wash & Sparkles
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(HighlighterPink.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NotebookSparkle(size = 14.dp, color = HighlighterYellow)
                        Text(
                            text = "مفكرة الحسابات (Style Lab)",
                            fontFamily = PatrickHandFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk
                        )
                        NotebookHeart(size = 13.dp, color = HighlighterPink)
                    }
                }

                // Small decorative doodle placeholder
                NotebookSparkle(size = 18.dp, color = HighlighterYellow)
            }
        }

        // --- Continuous Ruled Paper Canvas ---
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .verticalScroll(scrollState)
                    .drawBehind {
                        val gridPx = ruleSpacing.toPx()
                        var y = gridPx
                        while (y <= size.height) {
                            drawLine(
                                color = JournalRule.copy(alpha = 0.35f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 0.6.dp.toPx()
                            )
                            y += gridPx
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 0.dp) // Strictly ZERO vertical padding
            ) {
                // --- Row 0 (0..29dp): Introduction Note sitting on line 1 ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = "✎ كل كلمة وكل رقم مكتوب ومثبت مباشرة فوق السطر",
                        fontFamily = TajawalFamily,
                        fontSize = 13.5.sp,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = 5.0.dp)
                    )
                }

                // --- Row 1 (29..58dp): Empty ruled line ---
                Spacer(modifier = Modifier.height(ruleSpacing))

                // ==========================================
                // SECTION 1: Pure Written Ledger (حساب مكتوب فوق السطور)
                // ==========================================
                // --- Row 2 (58..87dp): Section 1 Header with Pink Highlighter & Dot Underline ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing)
                        .notebookUnderlineWithDot(
                            color = HighlighterPink,
                            dotOnRight = true,
                            yOffset = 6.5.dp
                        ),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NotebookHighlightedBadge(
                            text = "1. مصاريف الدار (الأسبوعية)",
                            highlighterColor = HighlighterPink.copy(alpha = 0.50f),
                            fontSize = 16.5.sp,
                            horizontalPadding = 10.dp,
                            verticalPadding = 3.5.dp,
                            textOffset = 0.5.dp,
                            modifier = Modifier.offset(y = 5.5.dp)
                        )
                        NotebookSparkle(
                            size = 14.dp,
                            color = HighlighterYellow,
                            modifier = Modifier.offset(y = (-2).dp)
                        )
                    }
                }

                // --- Row 3 (87..116dp): Line 1: Groceries ---
                ShowcaseRuledEntry(
                    index = 1,
                    label = "خضار وفواكه",
                    amount = "180",
                    unit = "درهم",
                    ruleSpacing = ruleSpacing,
                    isConfirmed = true
                )

                // --- Row 4 (116..145dp): Line 2: Meat & Poultry ---
                ShowcaseRuledEntry(
                    index = 2,
                    label = "لحم ودجاج وسردين",
                    amount = "220",
                    unit = "درهم",
                    ruleSpacing = ruleSpacing,
                    isConfirmed = true
                )

                // --- Row 5 (145..174dp): Line 3: Dairy & Bread ---
                ShowcaseRuledEntry(
                    index = 3,
                    label = "حليب وبيض وزبدة",
                    amount = "65",
                    unit = "درهم",
                    ruleSpacing = ruleSpacing,
                    isConfirmed = true
                )

                // --- Row 6 (174..203dp): Empty ruled line before Total ---
                Spacer(modifier = Modifier.height(ruleSpacing))

                // --- Row 7 (203..232dp): Total Band (Sitting directly on the line) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing)
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Right side (RTL start): Yellow highlighter "المجموع" with full vertical centering
                    NotebookHighlightedBadge(
                        text = "المجموع",
                        highlighterColor = HighlighterYellow.copy(alpha = 0.65f),
                        fontSize = 16.5.sp,
                        horizontalPadding = 12.dp,
                        verticalPadding = 3.5.dp,
                        textOffset = 0.5.dp,
                        modifier = Modifier.offset(y = 5.5.dp)
                    )

                    // Left side (RTL end): Amount with double pink pen underline
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.drawBehind {
                            val sw = 1.3.dp.toPx()
                            val y1 = size.height + 3.5.dp.toPx()
                            val y2 = size.height + 7.0.dp.toPx()
                            val startX = -4.dp.toPx()
                            val endX = size.width + 4.dp.toPx()
                            drawLine(
                                color = HighlighterPink,
                                start = Offset(startX, y1),
                                end = Offset(endX, y1),
                                strokeWidth = sw,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = HighlighterPink,
                                start = Offset(startX, y2),
                                end = Offset(endX, y2),
                                strokeWidth = sw,
                                cap = StrokeCap.Round
                            )
                        }
                    ) {
                        Text(
                            text = "465",
                            fontFamily = PatrickHandFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 3.0.dp)
                        )
                        Text(
                            text = "درهم",
                            fontFamily = TajawalFamily,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JournalMutedInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 2.5.dp)
                        )
                    }
                }

                // --- Rows 8 & 9 (232..290dp): Two blank ruled lines separation ---
                Spacer(modifier = Modifier.height(ruleSpacing * 2))

                // ==========================================
                // SECTION 2: Card Alternatives with Notebook Flourishes (بدائل الكارتات)
                // ==========================================
                // --- Row 10 (290..319dp): Header: Green Highlighter with wavy underline ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing)
                        .notebookWavyUnderline(
                            color = HighlighterGreen,
                            strokeWidth = 1.5.dp,
                            yOffset = 6.5.dp
                        ),
                    verticalAlignment = Alignment.Bottom
                ) {
                    NotebookHighlightedBadge(
                        text = "2. نماذج تعويض الكارتات بزواق الدفتر",
                        highlighterColor = HighlighterGreen.copy(alpha = 0.50f),
                        fontSize = 14.5.sp,
                        horizontalPadding = 10.dp,
                        verticalPadding = 3.5.dp,
                        textOffset = 0.5.dp,
                        modifier = Modifier.offset(y = 5.5.dp)
                    )
                }

                // --- Row 11 (319..348dp): Concept A Title ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = "• النموذج (أ): سطر متموج وسهم (بدون كارتة)",
                        fontFamily = TajawalFamily,
                        fontSize = 13.5.sp,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = 5.0.dp)
                    )
                }

                // --- Row 12 (348..377dp): Concept A Row ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing)
                        .notebookWavyUnderline(
                            color = HighlighterPink.copy(alpha = 0.75f),
                            strokeWidth = 1.4.dp,
                            yOffset = 4.0.dp
                        ),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "كراء الشقة والماء",
                            fontFamily = TajawalFamily,
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 6.7.dp)
                        )
                        Text(
                            text = "(3 سطور • اليوم)",
                            fontFamily = TajawalFamily,
                            fontSize = 12.sp,
                            color = JournalMutedInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 5.0.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "1 250",
                            fontFamily = PatrickHandFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = JournalInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 5.7.dp)
                        )
                        Text(
                            text = "درهم",
                            fontFamily = TajawalFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JournalMutedInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.offset(y = 5.0.dp)
                        )
                        Text(
                            text = "←",
                            fontFamily = PatrickHandFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighlighterPink,
                            modifier = Modifier.offset(y = 1.7.dp)
                        )
                    }
                }

                // --- Row 13 (377..406dp): Blank ruled line ---
                Spacer(modifier = Modifier.height(ruleSpacing))

                // --- Row 14 (406..435dp): Concept B Title ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = "• النموذج (ب): ورقة لاصقة (Sticky Note)",
                        fontFamily = TajawalFamily,
                        fontSize = 13.5.sp,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = 5.0.dp)
                    )
                }

                // --- Rows 15..18 (435..551dp): Concept B: Sticky Note (Exactly 4 lines tall) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing * 4)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    NotebookStickyNote(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color(0xFFFFF9DB),
                        tapeColor = HighlighterPink.copy(alpha = 0.8f),
                        rotationDegrees = -1.2f
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📌 تذكير ميزانية الشهر:",
                                fontFamily = TajawalFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalInk,
                                style = TextStyle(platformStyle = NoFontPadding)
                            )
                            NotebookHeart(size = 14.dp, color = HighlighterPink)
                        }
                        Text(
                            text = "بقات 350 درهم مخصصة للمصاريف الطارئة، ما تفوتهاش قبل نهار 30!",
                            fontFamily = TajawalFamily,
                            fontSize = 13.5.sp,
                            color = JournalWritingInk,
                            style = TextStyle(platformStyle = NoFontPadding),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // --- Row 19 (551..580dp): Blank ruled line ---
                Spacer(modifier = Modifier.height(ruleSpacing))

                // --- Row 20 (580..609dp): Concept C Title ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = "• النموذج (ج): إطار متقطع مرسوم باليد ومحكوم بالسطورة",
                        fontFamily = TajawalFamily,
                        fontSize = 13.5.sp,
                        color = JournalMutedInk,
                        style = TextStyle(platformStyle = NoFontPadding),
                        modifier = Modifier.offset(y = 5.0.dp)
                    )
                }

                // --- Rows 21..23 (609..696dp): Concept C: Sketched Dashed Box (Exactly 3 lines tall) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing * 3)
                        .journalDashedBorder(
                            color = JournalInk.copy(alpha = 0.45f),
                            strokeWidth = 1.2.dp,
                            cornerRadius = 8.dp
                        )
                        .padding(horizontal = 12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Line 1 inside box (29dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ruleSpacing),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            NotebookHighlightedBadge(
                                text = "سفر طنجة (حساب مشترك)",
                                highlighterColor = HighlighterBlue.copy(alpha = 0.50f),
                                fontSize = 15.5.sp,
                                horizontalPadding = 8.dp,
                                verticalPadding = 3.dp,
                                textOffset = 0.5.dp,
                                modifier = Modifier.offset(y = 5.5.dp)
                            )
                            Text(
                                text = "3 400 درهم",
                                fontFamily = PatrickHandFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = JournalInk,
                                style = TextStyle(platformStyle = NoFontPadding),
                                modifier = Modifier.offset(y = 5.7.dp)
                            )
                        }

                        // Line 2 inside box (29dp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ruleSpacing),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "أوتيل + مازوت + عشاء جماعي",
                                fontFamily = TajawalFamily,
                                fontSize = 13.sp,
                                color = JournalMutedInk,
                                style = TextStyle(platformStyle = NoFontPadding),
                                modifier = Modifier.offset(y = 5.0.dp)
                            )
                            Text(
                                text = "عرض التفاصيل ←",
                                fontFamily = TajawalFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HighlighterPink,
                                style = TextStyle(platformStyle = NoFontPadding),
                                modifier = Modifier.offset(y = 5.0.dp)
                            )
                        }
                    }
                }

                // --- Rows 24 & 25 (696..754dp): Two blank ruled lines separation ---
                Spacer(modifier = Modifier.height(ruleSpacing * 2))

                // ==========================================
                // SECTION 3: Bullet Journal Doodles Palette
                // ==========================================
                // --- Row 26 (754..783dp): Header ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing)
                        .notebookUnderlineWithDot(
                            color = HighlighterYellow,
                            dotOnRight = true,
                            yOffset = 6.5.dp
                        ),
                    verticalAlignment = Alignment.Bottom
                ) {
                    NotebookHighlightedBadge(
                        text = "3. رموز وتزويقات إضافية من الدفتر",
                        highlighterColor = HighlighterYellow.copy(alpha = 0.50f),
                        fontSize = 16.5.sp,
                        horizontalPadding = 10.dp,
                        verticalPadding = 3.5.dp,
                        textOffset = 0.5.dp,
                        modifier = Modifier.offset(y = 5.5.dp)
                    )
                }

                // --- Rows 27 & 28 (783..841dp): Doodles Row (Exactly 2 lines tall = 58dp) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ruleSpacing * 2),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        NotebookCircledNumber(number = 1)
                        NotebookCircledNumber(number = 2)
                        NotebookCircledNumber(number = 3)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NotebookCheckmark(size = 20.dp)
                        NotebookCross(size = 20.dp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NotebookSparkle(size = 18.dp)
                        NotebookHeart(size = 18.dp)
                    }
                }

                // Bottom padding in exact ruled multiples
                Spacer(modifier = Modifier.height(ruleSpacing * 3))
            }
        }
    }
}

/**
 * Single entry row sitting directly on an exact 29dp ruled line.
 * Calibrated optical alignment ensures both Arabic script and Latin digits rest on the ruled line.
 */
@Composable
private fun ShowcaseRuledEntry(
    index: Int,
    label: String,
    amount: String,
    unit: String,
    ruleSpacing: Dp,
    isConfirmed: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ruleSpacing)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Right side (in RTL): Circled Number + Arabic Label
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NotebookCircledNumber(
                number = index,
                size = 18.dp,
                circleColor = HighlighterBlue.copy(alpha = 0.7f),
                modifier = Modifier.offset(y = 1.3.dp)
            )
            Text(
                text = label,
                fontFamily = TajawalFamily,
                fontSize = 16.5.sp,
                fontWeight = FontWeight.Medium,
                color = JournalInk,
                style = TextStyle(platformStyle = NoFontPadding),
                modifier = Modifier.offset(y = 6.7.dp)
            )
        }

        // Left side (in RTL): Amount + Unit + Status Checkmark
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = amount,
                fontFamily = PatrickHandFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = JournalInk,
                style = TextStyle(platformStyle = NoFontPadding),
                modifier = Modifier.offset(y = 5.7.dp)
            )
            Text(
                text = unit,
                fontFamily = TajawalFamily,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = JournalMutedInk,
                style = TextStyle(platformStyle = NoFontPadding),
                modifier = Modifier.offset(y = 5.0.dp)
            )
            if (isConfirmed) {
                NotebookCheckmark(
                    size = 16.dp,
                    modifier = Modifier.offset(y = 1.7.dp)
                )
            }
        }
    }
}
