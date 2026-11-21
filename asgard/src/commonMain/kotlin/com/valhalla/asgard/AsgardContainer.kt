package com.valhalla.asgard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Draws a treatment inside a component's shaped Material surface, behind its content and padding. */
@Stable
interface AsgardContainer {
    /** Return a draw modifier. The component owns clipping, elevation, layout and interactions. */
    @Composable
    fun backgroundModifier(containerColor: Color): Modifier

    companion object {
        /** Uses the component's Material fill. */
        val Material: AsgardContainer = object : AsgardContainer {
            @Composable
            override fun backgroundModifier(containerColor: Color): Modifier = Modifier
        }
    }
}
