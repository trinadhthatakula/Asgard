package com.valhalla.asgard.demo

import androidx.compose.runtime.Composable

/** One showcase entry: metadata + a live preview + the usage snippet. */
data class ComponentEntry(
    val name: String,
    val category: String,
    val description: String,
    val code: String,
    val content: @Composable () -> Unit,
)
