package com.cash.guide.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.cash.guide.ui.notebook.Ink
import com.cash.guide.ui.notebook.InkTone
import com.cash.guide.ui.notebook.MutedInk
import com.cash.guide.ui.notebook.Paper
import com.cash.guide.ui.notebook.PaperWarm
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
    MaterialTheme(
        colorScheme = HisabiColors,
        content = content
    )
}
