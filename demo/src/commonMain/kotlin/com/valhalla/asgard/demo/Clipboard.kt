package com.valhalla.asgard.demo

/** Copies [text] to the system clipboard. Platform-provided (browser clipboard on wasmJs). */
expect fun copyToClipboard(text: String)
