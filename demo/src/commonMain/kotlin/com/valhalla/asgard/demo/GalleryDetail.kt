package com.valhalla.asgard.demo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.valhalla.asgard.components.AsgardSettingToggleRow
import kotlinx.coroutines.delay

@Composable
internal fun DetailPane(entry: ComponentEntry, platform: GalleryPlatform, compact: Boolean, onHome: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val focus = remember { FocusRequester() }
    var glass by rememberSaveable { mutableStateOf(false) }
    val rating = glassCompatibility.getValue(entry.glassComponent)
    LaunchedEffect(entry.id) { focus.requestFocus() }

    Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 960.dp).fillMaxWidth().padding(if (compact) 16.dp else 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FlowRow(verticalArrangement = Arrangement.Center) {
                TextButton(onClick = onHome) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Gallery")
                }
                Text("/  ${entry.category}", Modifier.align(Alignment.CenterVertically), style = type.labelMedium,
                    color = colors.onSurfaceVariant)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(entry.name, Modifier.focusRequester(focus).focusable().semantics { heading() },
                    style = if (compact) type.headlineMedium else type.displaySmall)
                Text(entry.description, style = type.bodyLarge, color = colors.onSurfaceVariant)
            }
            Surface(shape = MaterialTheme.shapes.extraLarge, color = colors.surfaceContainerLow,
                border = BorderStroke(1.dp, colors.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(if (compact) 12.dp else 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Live preview", Modifier.semantics { heading() }, style = type.titleLarge)
                    AsgardSettingToggleRow("Preview glass", glass, { glass = it }, subtitle = "Compatibility ${rating.score}/5",
                        modifier = Modifier.fillMaxWidth())
                    if (glass) Text(rating.guidance, style = type.bodySmall, color = colors.onSurfaceVariant)
                    HorizontalDivider(color = colors.outlineVariant)
                    CompositionLocalProvider(LocalPreviewGlass provides glass) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { entry.content() }
                    }
                }
            }
            ExampleCode(entry, platform)
        }
    }
}

@Composable
private fun ExampleCode(entry: ComponentEntry, platform: GalleryPlatform) {
    val colors = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    var copyState by remember(entry.id) { mutableStateOf("Copy") }
    LaunchedEffect(copyState) {
        if (copyState != "Copy" && copyState != "Copying…") {
            delay(2000)
            copyState = "Copy"
        }
    }
    Surface(color = colors.surfaceContainerHigh, shape = MaterialTheme.shapes.large) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Code, null, tint = colors.primary)
                Text("Minimal example", Modifier.weight(1f).semantics { heading() }, style = type.titleMedium)
                TextButton(enabled = copyState != "Copying…", onClick = {
                    copyState = "Copying…"
                    platform.copyToClipboard(entry.code) { success -> copyState = if (success) "Copied" else "Copy failed · retry" }
                }) { Text(copyState) }
            }
            HorizontalDivider(color = colors.outlineVariant)
            SelectionContainer {
                Box(Modifier.fillMaxWidth().heightIn(max = 280.dp).verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
                    Text(entry.code, style = type.bodySmall.copy(fontFamily = type.labelMedium.fontFamily))
                }
            }
        }
    }
}
