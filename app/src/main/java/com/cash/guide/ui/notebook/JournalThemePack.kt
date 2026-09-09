package com.cash.guide.ui.notebook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.cash.guide.R

enum class JournalThemeId {
    CLASSIC_YELLOW,
    KRAFT_VINTAGE,
    WHITE_NOTEBOOK,
    DARK_CARNET
}

data class JournalThemePalette(
    val id: JournalThemeId,
    val nameResId: Int,
    val descResId: Int,
    val paper: Color,
    val dockBg: Color,
    val ink: Color,
    val writingInk: Color,
    val mutedInk: Color,
    val rule: Color,
    val accent: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val isDark: Boolean = false
)

object JournalThemePacks {
    val ClassicYellow = JournalThemePalette(
        id = JournalThemeId.CLASSIC_YELLOW,
        nameResId = R.string.theme_classic_yellow_title,
        descResId = R.string.theme_classic_yellow_desc,
        paper = Color(0xFFFBF6E8),
        dockBg = Color(0xFFF6F0DF),
        ink = Color(0xFF242421),
        writingInk = Color(0xFF383834),
        mutedInk = Color(0xFF7A7972),
        rule = Color(0xFFB8C7CC),
        accent = Color(0xFFF3A7B9),
        cardBg = Color(0xFFFFFDF7),
        cardBorder = Color(0xFFE6DECA),
        isDark = false
    )

    val KraftVintage = JournalThemePalette(
        id = JournalThemeId.KRAFT_VINTAGE,
        nameResId = R.string.theme_kraft_vintage_title,
        descResId = R.string.theme_kraft_vintage_desc,
        paper = Color(0xFFEFE6D5),
        dockBg = Color(0xFFE4D8C3),
        ink = Color(0xFF2E2218),
        writingInk = Color(0xFF3D2F23),
        mutedInk = Color(0xFF7C6C5E),
        rule = Color(0xFFBCAC9B),
        accent = Color(0xFFD97706),
        cardBg = Color(0xFFF6EFE3),
        cardBorder = Color(0xFFD6C5B1),
        isDark = false
    )

    val WhiteNotebook = JournalThemePalette(
        id = JournalThemeId.WHITE_NOTEBOOK,
        nameResId = R.string.theme_white_notebook_title,
        descResId = R.string.theme_white_notebook_desc,
        paper = Color(0xFFF8F9FA),
        dockBg = Color(0xFFEEF2F6),
        ink = Color(0xFF143575),
        writingInk = Color(0xFF1E429F),
        mutedInk = Color(0xFF607290),
        rule = Color(0xFFA8C2D8),
        accent = Color(0xFFE02424),
        cardBg = Color(0xFFFFFFFF),
        cardBorder = Color(0xFFD1DCE5),
        isDark = false
    )

    val DarkCarnet = JournalThemePalette(
        id = JournalThemeId.DARK_CARNET,
        nameResId = R.string.theme_dark_carnet_title,
        descResId = R.string.theme_dark_carnet_desc,
        paper = Color(0xFF18191B),
        dockBg = Color(0xFF121314),
        ink = Color(0xFFF1EFEA),
        writingInk = Color(0xFFE3E1DB),
        mutedInk = Color(0xFF92918B),
        rule = Color(0xFF2C2E33),
        accent = Color(0xFFE5A93C),
        cardBg = Color(0xFF222428),
        cardBorder = Color(0xFF383B42),
        isDark = true
    )

    val allPacks: List<JournalThemePalette> = listOf(
        ClassicYellow,
        KraftVintage,
        WhiteNotebook,
        DarkCarnet
    )

    fun get(id: JournalThemeId): JournalThemePalette = when (id) {
        JournalThemeId.CLASSIC_YELLOW -> ClassicYellow
        JournalThemeId.KRAFT_VINTAGE -> KraftVintage
        JournalThemeId.WHITE_NOTEBOOK -> WhiteNotebook
        JournalThemeId.DARK_CARNET -> DarkCarnet
    }
}

val LocalJournalTheme = compositionLocalOf { JournalThemePacks.ClassicYellow }

object JournalTheme {
    var currentPalette: JournalThemePalette by mutableStateOf(JournalThemePacks.ClassicYellow)

    val colors: JournalThemePalette
        @Composable
        get() = LocalJournalTheme.current
}
