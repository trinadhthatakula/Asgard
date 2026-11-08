@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.valhalla.asgard.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberGalleryPlatform(): GalleryPlatform = remember { BrowserGalleryPlatform }

private object BrowserGalleryPlatform : GalleryPlatform {
    override fun copyToClipboard(text: String, onResult: (Boolean) -> Unit) = writeClipboard(text, onResult)
    override fun readEntry(): String? = readHash()
    override fun selectEntry(id: String) = writeHash(id)
    override fun observeEntry(onChanged: (String?) -> Unit): () -> Unit = watchHash(onChanged)
}
private fun writeClipboard(text: String, onResult: (Boolean) -> Unit): Unit = js("""{
    try {
        if (!navigator.clipboard) { onResult(false); return; }
        navigator.clipboard.writeText(text).then(() => onResult(true), () => onResult(false));
    } catch (_) { onResult(false); }
}""")
private fun readHash(): String? = js("{ try { return location.hash ? decodeURIComponent(location.hash.slice(1)) : null; } catch (_) { return null; } }")
private fun writeHash(id: String): Unit = js("{ location.hash = encodeURIComponent(id); }")
private fun watchHash(onChanged: (String?) -> Unit): () -> Unit = js("""{
    const listener = () => { try { onChanged(location.hash ? decodeURIComponent(location.hash.slice(1)) : null); } catch (_) { onChanged(null); } };
    window.addEventListener('hashchange', listener);
    return () => window.removeEventListener('hashchange', listener);
}""")
