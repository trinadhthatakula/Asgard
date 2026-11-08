@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.valhalla.asgard.demo

actual fun copyToClipboard(text: String, onResult: (Boolean) -> Unit) = writeClipboard(text, onResult)
private fun writeClipboard(text: String, onResult: (Boolean) -> Unit): Unit = js("""{
    try {
        if (!navigator.clipboard) { onResult(false); return; }
        navigator.clipboard.writeText(text).then(() => onResult(true), () => onResult(false));
    } catch (_) { onResult(false); }
}""")
actual fun readGalleryEntry(): String? = readHash()
private fun readHash(): String? = js("{ try { return location.hash ? decodeURIComponent(location.hash.slice(1)) : null; } catch (_) { return null; } }")
actual fun selectGalleryEntry(id: String) = writeHash(id)
private fun writeHash(id: String): Unit = js("{ location.hash = encodeURIComponent(id); }")
actual fun observeGalleryEntry(onChanged: (String?) -> Unit): () -> Unit = watchHash(onChanged)
private fun watchHash(onChanged: (String?) -> Unit): () -> Unit = js("""{
    const listener = () => { try { onChanged(location.hash ? decodeURIComponent(location.hash.slice(1)) : null); } catch (_) { onChanged(null); } };
    window.addEventListener('hashchange', listener);
    return () => window.removeEventListener('hashchange', listener);
}""")
