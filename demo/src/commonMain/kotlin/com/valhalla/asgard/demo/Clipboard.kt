package com.valhalla.asgard.demo

import androidx.compose.runtime.Composable

/** Gallery-only platform services; no platform state is part of the Asgard library. */
internal interface GalleryPlatform {
    fun onThemeChanged(dark: Boolean) {}
    fun copyToClipboard(text: String, onResult: (Boolean) -> Unit)
    fun readEntry(): String?
    fun selectEntry(id: String)
    fun observeEntry(onChanged: (String?) -> Unit): () -> Unit
}

@Composable
internal expect fun rememberGalleryPlatform(): GalleryPlatform
