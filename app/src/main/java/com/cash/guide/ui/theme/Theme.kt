package com.cash.guide.ui.theme

import android.app.Activity
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.cash.guide.ui.notebook.CreamFrothFamily
import com.cash.guide.ui.notebook.Ink
import com.cash.guide.ui.notebook.InkTone
import com.cash.guide.ui.notebook.MutedInk
import com.cash.guide.ui.notebook.NoFontPadding
import com.cash.guide.ui.notebook.Paper
import com.cash.guide.ui.notebook.PaperWarm
import com.cash.guide.ui.notebook.PatrickHandFamily
import com.cash.guide.ui.notebook.Rule

private val HisabiColors = lightColorScheme(
    primary = InkTone.Orange.color,
    onPrimary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperWarm,
    onSurfaceVariant = MutedInk,
    outline = Rule
)

@Composable
fun HisabiTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
            }
        }
    }

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val defaultFont = if (isRtl) CreamFrothFamily else PatrickHandFamily
    val defaultTextStyle = TextStyle(
        fontFamily = defaultFont,
        fontWeight = FontWeight.Normal,
        platformStyle = NoFontPadding
    )
    val hisabiTypography = Typography(
        displayLarge = defaultTextStyle.copy(fontSize = 30.sp),
        displayMedium = defaultTextStyle.copy(fontSize = 24.sp),
        displaySmall = defaultTextStyle.copy(fontSize = 20.sp),
        headlineLarge = defaultTextStyle.copy(fontSize = 19.sp),
        headlineMedium = defaultTextStyle.copy(fontSize = 17.5.sp),
        headlineSmall = defaultTextStyle.copy(fontSize = 16.sp),
        titleLarge = defaultTextStyle.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = defaultTextStyle.copy(fontSize = 15.5.sp, fontWeight = FontWeight.Medium),
        titleSmall = defaultTextStyle.copy(fontSize = 14.5.sp, fontWeight = FontWeight.Medium),
        bodyLarge = defaultTextStyle.copy(fontSize = 15.sp),
        bodyMedium = defaultTextStyle.copy(fontSize = 14.sp),
        bodySmall = defaultTextStyle.copy(fontSize = 12.5.sp),
        labelLarge = defaultTextStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
        labelMedium = defaultTextStyle.copy(fontSize = 12.5.sp),
        labelSmall = defaultTextStyle.copy(fontSize = 11.sp)
    )

    MaterialTheme(
        colorScheme = HisabiColors,
        typography = hisabiTypography
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides defaultTextStyle.copy(fontSize = 14.5.sp, color = Ink)
        ) {
            content()
        }
    }
}
