package com.valhalla.asgard.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.browser.window
import org.w3c.dom.events.Event

@Composable
internal actual fun platformAllowsFrost(): Boolean {
    val preference = remember { window.matchMedia("(prefers-reduced-transparency: reduce)") }
    var reduced by remember { mutableStateOf(preference.matches) }
    DisposableEffect(preference) {
        val listener: (Event) -> Unit = { reduced = preference.matches }
        preference.addEventListener("change", listener)
        reduced = preference.matches
        onDispose { preference.removeEventListener("change", listener) }
    }
    return !reduced
}
