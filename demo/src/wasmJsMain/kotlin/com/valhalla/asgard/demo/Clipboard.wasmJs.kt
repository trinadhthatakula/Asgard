package com.valhalla.asgard.demo

/** Browser clipboard write, guarded so an unsupported/blocked clipboard never throws. */
actual fun copyToClipboard(text: String) {
    writeClipboard(text)
}

private fun writeClipboard(text: String): Unit =
    js("{ if (typeof navigator !== 'undefined' && navigator.clipboard) { navigator.clipboard.writeText(text); } }")
