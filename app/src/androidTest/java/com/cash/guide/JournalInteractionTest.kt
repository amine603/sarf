package com.cash.guide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import android.os.Build
import android.view.WindowManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JournalInteractionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

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
    fun testCategoryHeaderAndAddButtonDisplayed() {
        composeTestRule.onNodeWithTag("tag_header_calculator_icon").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_add_row_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_total_result_band").assertIsDisplayed()
    }

    @Test
    fun testPopupOpenAndClose() {
        composeTestRule.onNodeWithTag("tag_header_calculator_icon").performClick()
        composeTestRule.onNodeWithTag("tag_popup_container").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_popup_close_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_popup_equals_key").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_popup_confirm_button").assertIsDisplayed().assertIsNotEnabled()

        composeTestRule.onNodeWithTag("tag_popup_close_button").performClick()
        composeTestRule.onNodeWithTag("tag_popup_container").assertDoesNotExist()
    }
}
