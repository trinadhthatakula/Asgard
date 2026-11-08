package com.valhalla.asgard.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

@Composable
internal actual fun rememberGalleryPlatform(): GalleryPlatform {
    val context = LocalContext.current
    val platform = remember(context) { AndroidGalleryPlatform(context) }
    BackHandler(enabled = platform.canGoBack) { platform.goBack() }
    return platform
}

private class AndroidGalleryPlatform(private val context: Context) : GalleryPlatform {
    private val preferences = context.getSharedPreferences("gallery", Context.MODE_PRIVATE)
    private val history = mutableStateListOf<String>()
    private val listeners = mutableSetOf<(String?) -> Unit>()
    val canGoBack get() = history.size > 1

    init { history.add(preferences.getString("entry", null) ?: "asgardheader") }

    override fun onThemeChanged(dark: Boolean) {
        val activity = generateSequence(context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>().firstOrNull() ?: return
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }

    override fun copyToClipboard(text: String, onResult: (Boolean) -> Unit) {
        val copied = runCatching {
            val clipboard = context.getSystemService(ClipboardManager::class.java)
                ?: error("Clipboard unavailable")
            clipboard.setPrimaryClip(ClipData.newPlainText("Asgard example", text))
        }.isSuccess
        onResult(copied)
    }

    override fun readEntry(): String? = history.lastOrNull()
    override fun selectEntry(id: String) {
        if (history.lastOrNull() != id) history.add(id)
        changed()
    }
    fun goBack() {
        if (canGoBack) { history.removeAt(history.lastIndex); changed() }
    }
    private fun changed() {
        val entry = readEntry()
        preferences.edit().putString("entry", entry).apply()
        listeners.toList().forEach { it(entry) }
    }
    override fun observeEntry(onChanged: (String?) -> Unit): () -> Unit {
        listeners.add(onChanged)
        return { listeners.remove(onChanged) }
    }
}
