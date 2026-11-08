package com.valhalla.asgard.app

import android.content.ClipboardManager
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.valhalla.asgard.demo.GalleryApp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GalleryDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentTestActivity>()

    @Test fun galleryBrowsesAndSearchesAllNewComponents() {
        compose.setContent { Box(Modifier.fillMaxSize().safeDrawingPadding()) { GalleryApp(initialEntry = "asgardavatar") } }
        compose.onNodeWithText("Browse").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Heatmap")
        compose.onNodeWithContentDescription("AsgardCalendarHeatmap").performClick()
        compose.onNodeWithText("Selected: 2026-11-08 · Value: 1.0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Add an entry for selected date").performClick()
        compose.onNodeWithText("Selected: 2026-11-08 · Value: 2.0").assertIsDisplayed()
    }

    @Test fun nativeClipboardCopiesActualExample() {
        compose.setContent { GalleryApp(initialEntry = "asgardavatar") }
        compose.onNodeWithText("Copy").performScrollTo().performClick()
        compose.onNodeWithText("Copied").assertExists()
        compose.runOnIdle {
            val clipboard = compose.activity.getSystemService(ClipboardManager::class.java)
            assertTrue(clipboard.primaryClip?.getItemAt(0)?.text.toString().contains("AsgardAvatar"))
        }
    }

    @Test fun androidBackRestoresPreviousGalleryEntry() {
        compose.setContent { GalleryApp(initialEntry = "asgardavatar") }
        compose.onNodeWithText("Browse").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Heatmap")
        compose.onNodeWithContentDescription("AsgardCalendarHeatmap").performClick()
        compose.onNodeWithText("AsgardCalendarHeatmap").assertExists()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("AsgardAvatar").assertExists()
    }

    @Test fun galleryAppearanceHasSingleNamedToggleTargets() {
        compose.setContent { GalleryApp(initialEntry = "navigation-variations") }
        compose.onNodeWithText("Theme").performClick()
        compose.onNodeWithText("Dark theme").performClick().assertIsOn()
        compose.onNodeWithText("Right-to-left layout").performClick().assertIsOn()
        compose.onAllNodes(isToggleable() and hasText("Dark theme")).assertCountEquals(1)
        compose.onAllNodes(isToggleable() and hasText("Right-to-left layout")).assertCountEquals(1)
    }
}
