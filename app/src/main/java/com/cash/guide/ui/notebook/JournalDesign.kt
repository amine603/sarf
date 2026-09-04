package com.cash.guide.ui.notebook

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R

// --- Color Palette: French Bullet-Journal ---
val JournalPaper = Color(0xFFFBF6E8)       // Warm cream paper
val JournalDockBg = Color(0xFFF6F0DF)      // Warm cream dock
val JournalInk = Color(0xFF242421)         // Graphite black ink
val JournalWritingInk = Color(0xFF383834)  // Slightly softer writing ink
val JournalMutedInk = Color(0xFF7A7972)    // Muted pencil ink
val JournalRule = Color(0xFFB8C7CC)        // Cool gray-blue ruling lines

// Pastel Marker Highlighters
val HighlighterPink = Color(0xFFF3A7B9)    // Category, '=' button, '+' dab, double underline
val HighlighterYellow = Color(0xFFF4D66D)  // 'Total' heading, '−' dab
val HighlighterGreen = Color(0xFFC9DDA0)   // '×' dab
val HighlighterBlue = Color(0xFFA8CFE3)    // '÷' dab

// Row Action Colors (Handwritten × and ✓)
val JournalActionDelete = Color(0xFFD66860)   // Restrained muted coral for delete '×'
val JournalActionConfirm = Color(0xFF3E8A52)  // Restrained muted green for confirm '✓'

// Action Icon Geometry (Unclipped, anchored directly on top of notebook paper rule like text)
val JournalActionIconSize = 12.dp
val JournalActionIconRuleGap = 0.dp

// Offline Handwritten Fonts
val JournalHandFamily = FontFamily(
    Font(R.font.patrick_hand_regular, FontWeight.Normal)
)

// Authoritative geometry token for ruled paper rhythm and rows
val JournalRuleSpacing = 48.dp

val NoFontPadding = PlatformTextStyle(includeFontPadding = false)

// --- Migrated Notebook & Theme Design System ---
val Paper = Color(0xFFFAF8F1)
val PaperWarm = Color(0xFFF5F0E7)
val Ink = Color(0xFF3B3C39)
val WritingInk = Color(0xFF62635E)
val MutedInk = Color(0xFF74736D)
val Rule = Color(0xFFBFD2D8)

val ColorCoral = Color(0xFFF05B48)
val ColorOrange = Color(0xFFE38D2C)

enum class InkTone(val color: Color) {
    Ink(Color(0xFF3B3C39)),
    Coral(Color(0xFFF05B48)),
    Teal(Color(0xFF2CA5A2)),
    Green(Color(0xFF59A85A)),
    Blue(Color(0xFF3377AD)),
    Purple(Color(0xFF8869B2)),
    Orange(Color(0xFFE38D2C))
}

object HisabiMetrics {
    val Grid = 29.dp
    val WritingBaseline = Grid
    val MetadataBaseline = Grid
    val Gutter = 44.dp
    val TopBarHeight = 54.dp
    val IconSize = 20.dp
    val KeypadDockHeight = 260.dp
}

// --- Bundled Offline Font Families ---
val PatrickHandFamily = JournalHandFamily

val TajawalFamily = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_medium, FontWeight.Medium),
    Font(R.font.tajawal_bold, FontWeight.Bold)
)

val ManropeFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

// --- Script Detection & Typography Helpers ---
fun isArabicScript(text: String): Boolean {
    return text.any { c ->
        c in '\u0600'..'\u06FF' ||
        c in '\u0750'..'\u077F' ||
        c in '\u08A0'..'\u08FF' ||
        c in '\uFB50'..'\uFDFF' ||
        c in '\uFE70'..'\uFEFF'
    }
}

@Composable
fun arabicWritingStyle(
    color: Color = WritingInk,
    sizeSp: Float = 14.5f,
    weight: FontWeight = FontWeight.Normal
) = TextStyle(
    fontFamily = TajawalFamily,
    fontSize = sizeSp.sp,
    lineHeight = with(LocalDensity.current) { HisabiMetrics.Grid.toSp() },
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    ),
    fontWeight = weight,
    color = color
)

@Composable
fun amountWritingStyle(
    color: Color = Ink,
    sizeSp: Float = 16.5f,
    weight: FontWeight = FontWeight.Medium
) = TextStyle(
    fontFamily = ManropeFamily,
    fontSize = sizeSp.sp,
    lineHeight = with(LocalDensity.current) { HisabiMetrics.Grid.toSp() },
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    ),
    fontWeight = weight,
    color = color
)

// --- Typography Styles ---
@Composable
fun journalCategoryStyle() = TextStyle(
    fontFamily = JournalHandFamily,
    fontSize = 18.sp,
    fontWeight = FontWeight.Normal,
    color = JournalInk,
    lineHeight = 22.sp,
    platformStyle = NoFontPadding
)

@Composable
fun journalRowNumberStyle() = TextStyle(
    fontFamily = JournalHandFamily,
    fontSize = 16.5.sp,
    fontWeight = FontWeight.Normal,
    color = JournalInk,
    lineHeight = 20.sp,
    platformStyle = NoFontPadding
)

@Composable
fun journalTitleStyle(text: String = "", color: Color = JournalInk): TextStyle {
    val isArabic = text.any { it in '\u0600'..'\u06FF' }
    return TextStyle(
        fontFamily = if (isArabic) TajawalFamily else JournalHandFamily,
        fontSize = if (isArabic) 18.sp else 16.5.sp,
        fontWeight = FontWeight.Normal,
        color = color,
        lineHeight = 20.sp,
        platformStyle = NoFontPadding
    )
}

@Composable
fun journalAmountStyle(color: Color = JournalInk) = TextStyle(
    fontFamily = JournalHandFamily,
    fontSize = 17.5.sp,
    fontWeight = FontWeight.Normal,
    color = color,
    lineHeight = 21.sp,
    platformStyle = NoFontPadding
)

@Composable
fun journalSuffixStyle() = TextStyle(
    fontFamily = JournalHandFamily,
    fontSize = 15.sp,
    fontWeight = FontWeight.Normal,
    color = JournalMutedInk,
    lineHeight = 18.sp,
    platformStyle = NoFontPadding
)

@Composable
fun journalTotalLabelStyle() = TextStyle(
    fontFamily = JournalHandFamily,
    fontSize = 16.5.sp,
    fontWeight = FontWeight.Normal,
    color = JournalInk,
    lineHeight = 20.sp,
    platformStyle = NoFontPadding
)

@Composable
fun journalPrimaryTotalStyle(color: Color = JournalInk) = TextStyle(
    fontFamily = JournalHandFamily,
    fontSize = 25.sp,
    fontWeight = FontWeight.Normal,
    color = color,
    lineHeight = 28.sp,
    platformStyle = NoFontPadding
)

@Composable
fun journalSecondaryTotalStyle() = TextStyle(
    fontFamily = JournalHandFamily,
    fontSize = 15.5.sp,
    fontWeight = FontWeight.Normal,
    color = JournalMutedInk,
    lineHeight = 19.sp,
    platformStyle = NoFontPadding
)

val JournalKeyDigitStyle = TextStyle(
    fontFamily = JournalHandFamily,
    fontSize = 22.sp,
    fontWeight = FontWeight.Normal,
    color = JournalInk,
    platformStyle = NoFontPadding
)

val JournalKeyOperatorStyle = TextStyle(
    fontFamily = JournalHandFamily,
    fontSize = 22.sp,
    fontWeight = FontWeight.Normal,
    color = JournalInk,
    platformStyle = NoFontPadding
)

// --- Deterministic Organic Highlighter Modifiers ---
/**
 * Organic highlighter stroke that visually centers around the text bounds
 * with balanced margin above and below, while preserving underlying baseline alignment.
 */
fun Modifier.journalHighlighter(
    color: Color,
    alpha: Float = 0.40f,
    horizontalPadding: Dp = 6.dp,
    verticalPadding: Dp = 1.0.dp,
    seedVariant: Int = 0
): Modifier = drawBehind {
    val hPad = horizontalPadding.toPx()
    val vPad = verticalPadding.toPx()
    val left = -hPad
    val top = -vPad
    val right = size.width + hPad
    val bottom = size.height + vPad
    val w = right - left
    val h = bottom - top

    // Deterministic organic path with tilted chisel entry/exit and subtle irregular upper/lower edges
    val p1 = if (seedVariant % 2 == 0) 0.6f else -0.6f
    val p2 = if (seedVariant % 3 == 0) -0.8f else 0.8f

    val path = Path().apply {
        moveTo(left + 2f, top + 1.2f)
        cubicTo(
            left + w * 0.28f, top - 1.0f + p1,
            left + w * 0.68f, top + 0.6f + p2,
            right - 1.5f, top + 1.2f
        )
        cubicTo(
            right + 2.0f, top + h * 0.45f,
            right + 1.0f, top + h * 0.85f,
            right - 1.8f, bottom - 0.9f
        )
        cubicTo(
            left + w * 0.72f, bottom + 1.0f + p1,
            left + w * 0.32f, bottom - 0.7f + p2,
            left + 2.2f, bottom - 1.2f
        )
        cubicTo(
            left - 1.2f, top + h * 0.65f,
            left - 0.6f, top + h * 0.35f,
            left + 2f, top + 1.2f
        )
        close()
    }

    drawPath(path = path, color = color.copy(alpha = alpha))
}

// Subtle active field underline
fun Modifier.journalActiveUnderline(
    color: Color,
    strokeWidth: Dp = 1.35.dp
): Modifier = drawBehind {
    val strokeW = strokeWidth.toPx()
    val y = size.height + 2.dp.toPx()
    drawLine(
        color = color,
        start = Offset(-2.dp.toPx(), y),
        end = Offset(size.width + 2.dp.toPx(), y),
        strokeWidth = strokeW,
        cap = StrokeCap.Round
    )
}

// Small pastel operator dab for keypad keys
fun Modifier.journalOperatorDab(
    color: Color,
    alpha: Float = 0.75f,
    widthDp: Dp = 32.dp,
    heightDp: Dp = 16.dp
): Modifier = drawBehind {
    val w = widthDp.toPx()
    val h = heightDp.toPx()
    val left = (size.width - w) / 2f
    val top = (size.height - h) / 2f
    val right = left + w
    val bottom = top + h

    val path = Path().apply {
        moveTo(left + 2f, top + 1.5f)
        quadraticTo(left + w * 0.5f, top - 1f, right - 1.5f, top + 1f)
        quadraticTo(right + 2.5f, top + h * 0.5f, right - 1f, bottom - 1f)
        quadraticTo(left + w * 0.5f, bottom + 1.5f, left + 1.5f, bottom - 1f)
        quadraticTo(left - 2.5f, top + h * 0.5f, left + 2f, top + 1.5f)
        close()
    }
    drawPath(path = path, color = color.copy(alpha = alpha))
}

// Double Pink Pen Underline beneath Total
@Composable
fun JournalDoubleUnderline(
    modifier: Modifier = Modifier,
    color: Color = HighlighterPink,
    width: Dp = 150.dp
) {
    Canvas(modifier = modifier.width(width).height(8.dp)) {
        val strokeW = 1.4.dp.toPx()
        // Upper line
        drawLine(
            color = color.copy(alpha = 0.88f),
            start = Offset(2.dp.toPx(), 2.dp.toPx()),
            end = Offset(size.width - 2.dp.toPx(), 2.dp.toPx()),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
        // Lower line
        drawLine(
            color = color.copy(alpha = 0.88f),
            start = Offset(5.dp.toPx(), 5.8.dp.toPx()),
            end = Offset(size.width - 5.dp.toPx(), 5.8.dp.toPx()),
            strokeWidth = strokeW,
            cap = StrokeCap.Round
        )
    }
}
