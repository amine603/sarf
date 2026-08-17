package com.cash.guide.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val Ivory = Color(0xFFF3F0E8)
val Paper = Color(0xFFFFFDFC)
val Charcoal = Color(0xFF232321)
val CharcoalRaised = Color(0xFF302F2C)
val Copper = Color(0xFFC9783A)
val CopperSoft = Color(0xFFE8C2A5)
val Ink = Color(0xFF28251F)
val MutedInk = Color(0xFF8A8378)
val Hairline = Color(0xFFE0DAD0)

private val HisabiColors = lightColorScheme(
    primary = Copper,
    onPrimary = Color.White,
    background = Ivory,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Color(0xFFECE7DE),
    onSurfaceVariant = MutedInk,
    outline = Hairline
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
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = HisabiColors,
        content = content
    )
}
