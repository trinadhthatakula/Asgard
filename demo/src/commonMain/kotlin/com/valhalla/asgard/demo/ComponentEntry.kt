package com.valhalla.asgard.demo

import androidx.compose.runtime.Composable

/** One showcase entry: metadata + a live preview + the usage snippet. */
data class ComponentEntry(
    val name: String,
    val category: String,
    val description: String,
    val code: String,
    val content: @Composable () -> Unit,
) {
    /** Name-derived IDs are part of the gallery URL contract; keep names stable. */
    val id: String = name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
}
