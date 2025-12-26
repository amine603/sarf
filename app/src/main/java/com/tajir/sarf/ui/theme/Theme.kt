package com.tajir.sarf.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import com.tajir.sarf.settings.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = SarfGreen,
    onPrimary = Color.White,
    primaryContainer = SarfGreenSoft,
    onPrimaryContainer = SarfText,

    background = SarfBg,
    onBackground = SarfText,
    surface = SarfSurface,
    onSurface = SarfText,

    outline = SarfOutline,
    outlineVariant = SarfOutline,
)

private val DarkColorScheme = darkColorScheme(
    primary = SarfGreen,
    onPrimary = Color.White,
    primaryContainer = SarfGreenStrong,
    onPrimaryContainer = Color.White,

    background = SarfBgDark,
    onBackground = SarfTextDark,
    surface = SarfSurfaceDark,
    onSurface = SarfTextDark,

    outline = SarfOutlineDark,
    outlineVariant = SarfOutlineDark,
)

@Composable
fun SarfTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Keep compileSdk checks to avoid unused import warnings for older templates.
    @Suppress("UNUSED_VARIABLE")
    val sdk = Build.VERSION.SDK_INT
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = SarfShapes,
        content = {
            val locale = LocalConfiguration.current.locales[0]
            val isArabic = locale.language == "ar"
            CompositionLocalProvider(
                LocalLayoutDirection provides (if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr)
            ) {
                content()
            }
        }
    )
}