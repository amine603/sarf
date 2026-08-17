package com.cash.guide.ui.notebook

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cash.guide.R

// --- Centralized MyNotes Color System ---
val Paper = Color(0xFFFAF8F1)
val PaperWarm = Color(0xFFF5F0E7)
val Ink = Color(0xFF3B3C39)
val WritingInk = Color(0xFF62635E)
val MutedInk = Color(0xFF74736D)
val Rule = Color(0xFFBFD2D8)

val ColorCoral = Color(0xFFF05B48)
val ColorTeal = Color(0xFF2CA5A2)
val ColorGreen = Color(0xFF59A85A)
val ColorBlue = Color(0xFF3377AD)
val ColorPurple = Color(0xFF8869B2)
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
val PatrickHandFamily = FontFamily(
    Font(R.font.patrick_hand_regular, FontWeight.Normal)
)

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

val KalamLightFamily = FontFamily(
    Font(R.font.kalam_light, FontWeight.Light)
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
fun notebookTitleStyle(
    text: String,
    color: Color = WritingInk
): TextStyle {
    val isArabic = isArabicScript(text)
    return TextStyle(
        fontFamily = if (isArabic) TajawalFamily else PatrickHandFamily,
        fontSize = if (isArabic) 14.5.sp else 17.5.sp,
        lineHeight = with(LocalDensity.current) { HisabiMetrics.Grid.toSp() },
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None
        ),
        fontWeight = FontWeight.Normal,
        color = color
    )
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

val InterfaceSmall = TextStyle(
    fontFamily = ManropeFamily,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.Medium,
    color = MutedInk
)

val InterfaceTopBarTitle = TextStyle(
    fontFamily = TajawalFamily,
    fontSize = 20.sp,
    lineHeight = 24.sp,
    fontWeight = FontWeight.Medium,
    color = Ink
)

val KeypadDigitStyle = TextStyle(
    fontFamily = ManropeFamily,
    fontSize = 21.sp,
    lineHeight = 26.sp,
    fontWeight = FontWeight.Medium,
    color = Ink
)

val KeypadOperatorStyle = TextStyle(
    fontFamily = ManropeFamily,
    fontSize = 21.sp,
    lineHeight = 26.sp,
    fontWeight = FontWeight.Medium,
    color = ColorOrange
)
