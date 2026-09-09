package com.cash.guide

import com.cash.guide.ui.notebook.JournalInk
import com.cash.guide.ui.notebook.JournalPaper
import com.cash.guide.ui.notebook.JournalTheme
import com.cash.guide.ui.notebook.JournalThemeId
import com.cash.guide.ui.notebook.JournalThemePacks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalThemeTest {

    @Test
    fun testAllThemePacksDefinedAndRetrievable() {
        assertEquals(4, JournalThemePacks.allPacks.size)

        for (id in JournalThemeId.entries) {
            val pack = JournalThemePacks.get(id)
            assertEquals(id, pack.id)
        }
    }

    @Test
    fun testDarkCarnetIsDarkTrueOthersFalse() {
        assertFalse(JournalThemePacks.ClassicYellow.isDark)
        assertFalse(JournalThemePacks.KraftVintage.isDark)
        assertFalse(JournalThemePacks.WhiteNotebook.isDark)
        assertTrue(JournalThemePacks.DarkCarnet.isDark)
    }

    @Test
    fun testUpdatingCurrentPaletteDynamicallyChangesColors() {
        // Switch to Kraft
        JournalTheme.currentPalette = JournalThemePacks.KraftVintage
        assertEquals(JournalThemePacks.KraftVintage.paper, JournalPaper)
        assertEquals(JournalThemePacks.KraftVintage.ink, JournalInk)

        // Switch to Dark Carnet
        JournalTheme.currentPalette = JournalThemePacks.DarkCarnet
        assertEquals(JournalThemePacks.DarkCarnet.paper, JournalPaper)
        assertEquals(JournalThemePacks.DarkCarnet.ink, JournalInk)

        // Switch back to Classic Yellow
        JournalTheme.currentPalette = JournalThemePacks.ClassicYellow
        assertEquals(JournalThemePacks.ClassicYellow.paper, JournalPaper)
        assertEquals(JournalThemePacks.ClassicYellow.ink, JournalInk)
    }
}
