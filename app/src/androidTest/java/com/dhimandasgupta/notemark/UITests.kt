package com.dhimandasgupta.notemark

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.dhimandasgupta.notemark.ui.designsystem.ThreeBouncingDots
import com.dhimandasgupta.notemark.ui.designsystem.ThreeBouncingDotsTag
import com.dhimandasgupta.notemark.util.assertRecompositionCount
import org.junit.Rule
import org.junit.Test

class UITests {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun ThreeBouncingDotsShouldRecomposeOnce() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            ThreeBouncingDots()
        }
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.onNodeWithTag(testTag = ThreeBouncingDotsTag.THREE_BOUNCING_DOTS).assertIsDisplayed()
        composeTestRule.onNodeWithTag(testTag = ThreeBouncingDotsTag.THREE_BOUNCING_DOTS).assertRecompositionCount(1)
    }

    @Test
    fun ThreeBouncingDotsShouldRecomposeOnceAfter6Seconds() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            ThreeBouncingDots()
        }
        composeTestRule.mainClock.advanceTimeBy(6_000)
        composeTestRule.onNodeWithTag(testTag = ThreeBouncingDotsTag.THREE_BOUNCING_DOTS).assertIsDisplayed()
        composeTestRule.onNodeWithTag(testTag = ThreeBouncingDotsTag.THREE_BOUNCING_DOTS).assertRecompositionCount(1)
    }

    @Test
    fun ThreeBouncingDotsShouldRecomposeOnceAfter10Seconds() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            ThreeBouncingDots()
        }
        composeTestRule.mainClock.advanceTimeBy(10_000)
        composeTestRule.onNodeWithTag(testTag = ThreeBouncingDotsTag.THREE_BOUNCING_DOTS).assertIsDisplayed()
        composeTestRule.onNodeWithTag(testTag = ThreeBouncingDotsTag.THREE_BOUNCING_DOTS).assertRecompositionCount(1)
    }
}