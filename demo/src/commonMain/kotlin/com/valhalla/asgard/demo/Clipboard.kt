package com.valhalla.asgard.demo

/** Reports completion, including denied or unsupported clipboard access. */
expect fun copyToClipboard(text: String, onResult: (Boolean) -> Unit)
expect fun readGalleryEntry(): String?
expect fun selectGalleryEntry(id: String)
expect fun observeGalleryEntry(onChanged: (String?) -> Unit): () -> Unit
