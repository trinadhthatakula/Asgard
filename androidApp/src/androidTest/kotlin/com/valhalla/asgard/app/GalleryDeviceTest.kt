package com.valhalla.asgard.app

import android.content.ClipboardManager
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.valhalla.asgard.demo.GalleryApp
import com.valhalla.asgard.demo.asgardCatalog
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GalleryDeviceTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentTestActivity>()

    @Test fun indexRestoresCategoryAndSearchAfterOpeningAnExample() {
        compose.setContent { GalleryApp(initialEntry = "gallery") }
        compose.onNodeWithText("All categories").performScrollTo().performClick()
        compose.onNodeWithText("Charts").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("calendar")
        compose.onNodeWithText("Calendar Heatmap").performScrollTo().performClick()
        compose.onNodeWithText("AsgardCalendarHeatmap").assertExists()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNode(hasSetTextAction()).assertTextContains("calendar")
        compose.onNodeWithText("Calendar Heatmap").assertExists()
        compose.onNode(hasSetTextAction()).performTextReplacement("AsgardAvatar")
        compose.onNodeWithText("No matching components").assertExists()
        compose.onNodeWithText("Clear filters").performScrollTo().performSemanticsAction(SemanticsActions.OnClick)
        compose.onNode(hasScrollToIndexAction()).performScrollToKey("index")
        compose.onNodeWithText("All categories").assertIsDisplayed()
    }

    @Test fun galleryBrowsesAndSearchesAllNewComponents() {
        compose.setContent { Box(Modifier.fillMaxSize().safeDrawingPadding()) { GalleryApp(initialEntry = "asgardavatar") } }
        compose.onNodeWithText("Browse").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Heatmap")
        compose.onNodeWithContentDescription("AsgardCalendarHeatmap").performClick()
        compose.onNodeWithText("Selected: 2026-11-08 · Value: 1.0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Add an entry for selected date").performScrollTo().performClick()
        compose.onNodeWithText("Selected: 2026-11-08 · Value: 2.0").performScrollTo().assertIsDisplayed()
    }

    @Test fun everyCatalogEntryCanPreviewItsRecommendedGlassTreatment() {
        var id by mutableStateOf(asgardCatalog.first().id)
        compose.setContent { GalleryApp(initialEntry = id) }
        for (entry in asgardCatalog) {
            compose.runOnIdle { id = entry.id }
            compose.onNodeWithText("Preview glass").performScrollTo().assertIsOff().performClick().assertIsOn()
        }
    }

    @Test fun glassChoiceFollowsItsEntryAndKeepsTheSampleInteractive() {
        compose.setContent { GalleryApp(initialEntry = "asgardstattile-animated") }
        val preview = compose.onNodeWithText("Preview glass")
        preview.assertIsOff().performClick().assertIsOn()
        compose.onNodeWithText("Steps today").performScrollTo().performClick()
        compose.onNodeWithText("250").assertExists()
        preview.performScrollTo().performClick().assertIsOff()
        compose.onNodeWithText("250").assertExists()
        preview.performClick()
        compose.onNodeWithText("Browse").performClick()
        compose.onNode(hasSetTextAction()).performTextInput("Avatar")
        compose.onNodeWithContentDescription("AsgardAvatar").performClick()
        preview.assertIsOff()
        compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
        preview.assertIsOn()
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
