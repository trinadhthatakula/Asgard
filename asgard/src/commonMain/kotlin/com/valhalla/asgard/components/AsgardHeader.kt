package com.valhalla.asgard.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The signature Asgard screen header: a left-aligned row of an optional leading
 * brand [icon] (or a back button) followed by a heavy, tightly-tracked title in
 * the theme's primary color, with a trailing [actions] slot.
 *
 * It deliberately fixes the title's weight/letter-spacing/color — that recipe IS
 * the recognizable look. Everything else (the actual colors and fonts) is inherited
 * from the host app's [MaterialTheme], so the same header reads as each app's brand.
 *
 * There is intentionally no Material `TopAppBar`; place this at the top of a screen
 * body over the screen background.
 *
 * ```kotlin
 * AsgardHeader(
 *     title = "Clusters",
 *     icon = Icons.Default.Dashboard,
 *     actions = {
 *         ConnectedButtonGroup(items = tabs, selectedIndex = index, onItemSelected = ::onTab)
 *     },
 * )
 * ```
 *
 * @param title the screen title.
 * @param icon optional leading icon, tinted with `primary`. Ignored when [onNavigateBack] is set.
 * @param onNavigateBack if non-null, shows a leading back button instead of [icon].
 * @param actions trailing content (e.g. a [ConnectedButtonGroup]), laid out at the end of the row.
 * @param titleMaxLines maximum number of lines for the [title]. Defaults to `1`.
 * @param titleOverflow how visual overflow of the [title] is handled. Defaults to [TextOverflow.Ellipsis].
 * @param titleStyle overrides the title [TextStyle]. When `null` (default) the signature recipe is used:
 *   `headlineMedium` with [FontWeight.Black] weight and `(-1).sp` letter-spacing. A non-null style is
 *   applied verbatim, so any weight/letter-spacing must be baked into the supplied style.
 * @param titleColor overrides the title color. When `null` (default) the theme `primary` color is used.
 * @param contentPadding padding applied around the header row. Defaults to `horizontal = 24.dp, vertical = 16.dp`.
 * @param backContentDescription content description for the back button glyph shown when [onNavigateBack]
 *   is set. Defaults to `"Back"`.
 * @param iconContentDescription content description for the leading [icon]. Defaults to `null`.
 * @param leading optional custom leading slot. When non-null it takes precedence over the back button /
 *   [icon] branch and is rendered in their place.
 */
@Composable
fun AsgardHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onNavigateBack: (() -> Unit)? = null,
    titleMaxLines: Int = 1,
    titleOverflow: TextOverflow = TextOverflow.Ellipsis,
    titleStyle: TextStyle? = null,
    titleColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    backContentDescription: String = "Back",
    iconContentDescription: String? = null,
    leading: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            leading != null -> {
                leading()
                Spacer(Modifier.width(8.dp))
            }

            onNavigateBack != null -> {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = AsgardBackArrow,
                        contentDescription = backContentDescription,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
            }
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = titleStyle ?: MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
            ),
            color = titleColor ?: MaterialTheme.colorScheme.primary,
            maxLines = titleMaxLines,
            overflow = titleOverflow,
        )

        actions()
    }
}

/**
 * Self-contained back-arrow used as the default [AsgardHeader] navigation glyph, so the library
 * does not depend on `material-icons-core`/`-extended`. `autoMirror` flips it under RTL, matching
 * Material's `Icons.AutoMirrored.Filled.ArrowBack`. Consumers can always supply their own icon.
 */
private val AsgardBackArrow: ImageVector by lazy {
    ImageVector.Builder(
        name = "AsgardBackArrow",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = true,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(20f, 11f)
            horizontalLineTo(7.83f)
            lineToRelative(5.59f, -5.59f)
            lineTo(12f, 4f)
            lineToRelative(-8f, 8f)
            lineToRelative(8f, 8f)
            lineToRelative(1.41f, -1.41f)
            lineTo(7.83f, 13f)
            horizontalLineTo(20f)
            verticalLineTo(11f)
            close()
        }
    }.build()
}
