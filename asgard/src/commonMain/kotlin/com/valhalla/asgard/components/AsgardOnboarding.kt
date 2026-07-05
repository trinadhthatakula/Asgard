package com.valhalla.asgard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A feature/benefit row: a tinted circular [icon] badge beside a [title] and optional
 * [description]. Common in onboarding, benefit lists, and feature callouts.
 *
 * @param icon the leading icon.
 * @param title the feature headline.
 * @param modifier the [Modifier] applied to the row.
 * @param description optional supporting text.
 * @param iconTint the icon color.
 * @param iconBackground the circular badge background color.
 * @param titleMaxLines the maximum number of lines for the [title].
 * @param titleOverflow how visual overflow of the [title] is handled.
 * @param descriptionMaxLines the maximum number of lines for the [description].
 * @param descriptionOverflow how visual overflow of the [description] is handled.
 * @param titleStyle the [TextStyle] applied to the [title].
 * @param descriptionStyle the [TextStyle] applied to the [description].
 * @param titleColor the color of the [title].
 * @param descriptionColor the color of the [description].
 * @param badgeSize the size of the circular icon badge.
 * @param iconSize the size of the [icon] inside the badge.
 * @param spacing the horizontal gap between the badge and the text column.
 * @param badgeShape the [Shape] of the icon badge.
 * @param onClick optional click handler; when set, the row becomes clickable.
 * @param iconContentDescription optional content description for the [icon].
 */
@Composable
fun AsgardFeatureRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconBackground: Color = MaterialTheme.colorScheme.primaryContainer,
    titleMaxLines: Int = Int.MAX_VALUE,
    titleOverflow: TextOverflow = TextOverflow.Ellipsis,
    descriptionMaxLines: Int = Int.MAX_VALUE,
    descriptionOverflow: TextOverflow = TextOverflow.Ellipsis,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
    descriptionStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    descriptionColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    badgeSize: Dp = 44.dp,
    iconSize: Dp = 24.dp,
    spacing: Dp = 16.dp,
    badgeShape: Shape = CircleShape,
    onClick: (() -> Unit)? = null,
    iconContentDescription: String? = null,
) {
    Row(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button) { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(badgeShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                tint = iconTint,
                modifier = Modifier.size(iconSize),
            )
        }
        Spacer(Modifier.width(spacing))
        Column {
            Text(
                text = title,
                style = titleStyle,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = titleMaxLines,
                overflow = titleOverflow,
            )
            if (description != null) {
                Text(
                    text = description,
                    style = descriptionStyle,
                    color = descriptionColor,
                    maxLines = descriptionMaxLines,
                    overflow = descriptionOverflow,
                )
            }
        }
    }
}

/**
 * A simple onboarding page scaffold: a [title], optional [subtitle], a [content] body slot, and
 * an optional bottom [actions] row (e.g. Back/Next buttons).
 *
 * @param title the page headline.
 * @param modifier the [Modifier] applied to the scaffold.
 * @param subtitle optional supporting text under the title.
 * @param titleMaxLines the maximum number of lines for the [title].
 * @param titleOverflow how visual overflow of the [title] is handled.
 * @param subtitleMaxLines the maximum number of lines for the [subtitle].
 * @param subtitleOverflow how visual overflow of the [subtitle] is handled.
 * @param titleStyle the [TextStyle] applied to the [title].
 * @param subtitleStyle the [TextStyle] applied to the [subtitle].
 * @param titleColor the color of the [title].
 * @param subtitleColor the color of the [subtitle].
 * @param contentPadding the padding applied around the scaffold content.
 * @param actionsArrangement the horizontal arrangement of the bottom [actions] row.
 * @param horizontalAlignment the horizontal alignment of the scaffold column children.
 * @param actions optional bottom action row content (laid out end-aligned in a [Row]).
 * @param content the page body.
 */
@Composable
fun AsgardOnboardingScaffold(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    titleMaxLines: Int = Int.MAX_VALUE,
    titleOverflow: TextOverflow = TextOverflow.Ellipsis,
    subtitleMaxLines: Int = Int.MAX_VALUE,
    subtitleOverflow: TextOverflow = TextOverflow.Ellipsis,
    titleStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    subtitleStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    actionsArrangement: Arrangement.Horizontal = Arrangement.End,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(contentPadding),
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = title,
            style = titleStyle,
            fontWeight = FontWeight.Bold,
            color = titleColor,
            maxLines = titleMaxLines,
            overflow = titleOverflow,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = subtitleStyle,
                color = subtitleColor,
                maxLines = subtitleMaxLines,
                overflow = subtitleOverflow,
            )
        }
        Spacer(Modifier.height(24.dp))
        content()
        if (actions != null) {
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = actionsArrangement,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions()
            }
        }
    }
}
