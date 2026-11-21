package com.valhalla.asgard.glass

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

// Source capture on Android 12 can repeat on unrelated snapshot writes. Keep this tier solid
// until it has a frame budget measured on physical hardware.
@Composable
internal actual fun platformAllowsFrost(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && LocalView.current.isHardwareAccelerated
