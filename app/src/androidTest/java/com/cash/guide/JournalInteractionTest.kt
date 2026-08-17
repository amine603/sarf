package com.cash.guide

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JournalInteractionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testCategoryHeaderAndAddButtonDisplayed() {
        composeTestRule.onNodeWithTag("tag_header_calculator_icon").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_add_row_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_compact_keypad").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag_total_section").assertIsDisplayed()
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
