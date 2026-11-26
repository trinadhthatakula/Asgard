package com.valhalla.asgard.demo

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // A returning tab may still have the previous HTML shell cached.
    val gallery = document.getElementById("gallery") as? HTMLElement ?: document.body!!
    ComposeViewport(gallery) {
        GalleryApp()
        LaunchedEffect(Unit) { document.getElementById("startup")?.remove() }
    }
}
