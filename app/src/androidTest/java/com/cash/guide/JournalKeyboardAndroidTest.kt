package com.cash.guide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import android.graphics.Bitmap
import android.os.Build
import android.view.WindowManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class JournalKeyboardAndroidTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    private fun captureUiScreenshot(filename: String) {
        val bitmap = try {
            composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        } catch (e: Exception) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        }
        if (bitmap != null) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val dir = context.getExternalFilesDir(null) ?: context.cacheDir
            val file = File(dir, filename)
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
    }

    @Before
    fun setUpDevice() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.executeShellCommand("input keyevent KEYCODE_WAKEUP")
        instrumentation.uiAutomation.executeShellCommand("wm dismiss-keyguard")
        composeTestRule.activityRule.scenario.onActivity { activity ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                activity.setShowWhenLocked(true)
                activity.setTurnScreenOn(true)
            }
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun testKeyboardLanguageSwitcher_cyclesLanguages() {
        // Tap on Row 1 title to open custom text keyboard
        composeTestRule.onNodeWithTag("tag_row_title_1").performClick()
        composeTestRule.waitForIdle()

        // Verify text keyboard dock is displayed
        composeTestRule.onNodeWithTag("tag_text_keypad").assertIsDisplayed()

        // Cycle language FR -> EN
        composeTestRule.onNodeWithTag("tag_language_key_top").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag_key_en_q").assertIsDisplayed()

        // Cycle language EN -> AR
        composeTestRule.onNodeWithTag("tag_language_key_top").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag_key_ar_ض").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_key_ar_د").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_key_ar_ذ").assertIsDisplayed()

        // Cycle language AR -> FR
        composeTestRule.onNodeWithTag("tag_language_key_top").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag_key_fr_a").assertIsDisplayed()
    }

    @Test
    fun testSwitchToNumericAndBackToText() {
        // Focus on title
        composeTestRule.onNodeWithTag("tag_row_title_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag_text_keypad").assertIsDisplayed()

        // Tap "123" to switch to compact numeric dock
        composeTestRule.onNodeWithTag("tag_switch_to_num_key_top").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag_compact_keypad").assertIsDisplayed()

        // Tap "ABC" to switch back to text keyboard dock
        composeTestRule.onNodeWithTag("tag_switch_to_text_key").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag_text_keypad").assertIsDisplayed()
    }

    private fun ensureArabicKeyboard() {
        for (i in 0..2) {
            val isArabicDisplayed = runCatching {
                composeTestRule.onNodeWithTag("tag_key_ar_ض").assertIsDisplayed()
            }.isSuccess
            if (isArabicDisplayed) break
            composeTestRule.onNodeWithTag("tag_language_key_top").performClick()
            composeTestRule.waitForIdle()
        }
    }

    @Test
    fun testArabicTyping_Khadija_and_Backspace() {
        // Focus title on Row 1
        composeTestRule.onNodeWithTag("tag_row_title_1").performClick()
        composeTestRule.waitForIdle()

        // Switch to Arabic
        ensureArabicKeyboard()

        // Clear any existing text first using backspace
        repeat(15) {
            composeTestRule.onNodeWithTag("tag_arabic_backspace").performClick()
        }
        composeTestRule.waitForIdle()

        // Type خديجة
        composeTestRule.onNodeWithTag("tag_key_ar_خ").performClick()
        composeTestRule.onNodeWithTag("tag_key_ar_د").performClick()
        composeTestRule.onNodeWithTag("tag_key_ar_ي").performClick()
        composeTestRule.onNodeWithTag("tag_key_ar_ج").performClick()
        composeTestRule.onNodeWithTag("tag_key_ar_ة").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tag_row_title_1").assertTextContains("خديجة")

        // Test Backspace
        composeTestRule.onNodeWithTag("tag_arabic_backspace").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tag_row_title_1").assertTextContains("خديج")
    }

    @Test
    fun testArabicTyping_Dhahab_WithPrimaryDhal() {
        // Focus title on Row 1
        composeTestRule.onNodeWithTag("tag_row_title_1").performClick()
        composeTestRule.waitForIdle()

        // Switch to Arabic
        ensureArabicKeyboard()

        // Clear any existing text
        repeat(15) {
            composeTestRule.onNodeWithTag("tag_arabic_backspace").performClick()
        }
        composeTestRule.waitForIdle()

        // Type ذهب (tests primary key ذ, ه, ب)
        composeTestRule.onNodeWithTag("tag_key_ar_ذ").performClick()
        composeTestRule.onNodeWithTag("tag_key_ar_ه").performClick()
        composeTestRule.onNodeWithTag("tag_key_ar_ب").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tag_row_title_1").assertTextContains("ذهب")
    }

    @Test
    fun testMoroccanEmojiPack_quickBarAndFullPanel() {
        var insertedText = ""
        var isNumeric = false
        var confirmed = false

        composeTestRule.setContent {
            com.cash.guide.ui.theme.HisabiTheme {
                com.cash.guide.ui.notebook.JournalTextKeyboardDock(
                    language = com.cash.guide.domain.JournalKeyboardLanguage.FRENCH,
                    shiftMode = com.cash.guide.domain.JournalShiftMode.OFF,
                    expanded = true,
                    onToggleExpand = {},
                    onCycleLanguage = {},
                    onSelectLanguage = {},
                    onToggleShift = {},
                    onInsertText = { text -> insertedText += text },
                    onBackspace = { if (insertedText.isNotEmpty()) insertedText = insertedText.dropLast(1) },
                    onSwitchToNumericMode = { isNumeric = true },
                    onConfirm = { confirmed = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        // 1. Verify text keyboard, 😊 key, and quick emoji bar are visible
        composeTestRule.onNodeWithTag("tag_text_keypad").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_key_emoji_switch").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_quick_emoji_🛒").assertIsDisplayed()
        captureUiScreenshot("screen_kb_quick_emoji.png")

        // 2. Click a quick emoji (e.g. ☕)
        composeTestRule.onNodeWithTag("tag_quick_emoji_☕").performClick()
        composeTestRule.waitForIdle()
        org.junit.Assert.assertEquals("☕", insertedText)

        // 3. Click 😊 key in row 4 to open full emoji panel
        composeTestRule.onNodeWithTag("tag_key_emoji_switch").performClick()
        composeTestRule.waitForIdle()

        // Verify full emoji panel is displayed with category tabs
        composeTestRule.onNodeWithTag("tag_emoji_panel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_emoji_cat_commerce").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_emoji_cat_chantier").assertIsDisplayed()
        captureUiScreenshot("screen_kb_emoji_panel.png")

        // 4. Switch to Chantier category
        composeTestRule.onNodeWithTag("tag_emoji_cat_chantier").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag_emoji_item_🏗️").assertIsDisplayed()
        captureUiScreenshot("screen_kb_emoji_chantier.png")

        // 5. Click emoji 🏗️ to insert
        composeTestRule.onNodeWithTag("tag_emoji_item_🏗️").performClick()
        composeTestRule.waitForIdle()
        org.junit.Assert.assertEquals("☕🏗️", insertedText)

        // 6. Switch back to ABC via bottom utility bar
        composeTestRule.onNodeWithTag("tag_emoji_to_abc").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag_key_fr_a").assertIsDisplayed()
        captureUiScreenshot("screen_kb_back_to_abc.png")
    }
}
