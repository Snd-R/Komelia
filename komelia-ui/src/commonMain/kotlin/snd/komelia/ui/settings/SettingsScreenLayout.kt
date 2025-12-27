package snd.komelia.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import kotlin.math.roundToInt

@Composable
 fun SettingsScreenLayout(
    navMenu: @Composable () -> Unit,
    content: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
) = Layout(
    modifier = Modifier.fillMaxSize(),
    contents = listOf(navMenu, content, dismissButton)
) { (navMenuMeasurable, contentMeasurable, dismissMeasurable), constraints ->
    val navWidth = settingsDesktopNavMenuWidth.roundToPx()
    val contentWidth = settingsDesktopContentWidth.roundToPx()
    val padding =
        ((constraints.maxWidth - (navWidth + contentWidth)).toFloat() / 2).roundToInt().coerceAtLeast(0)

    val contentPlaceable = contentMeasurable.first()
        .measure(
            constraints.copy(
                minWidth = 0,
                maxWidth = padding + contentWidth.coerceAtMost(constraints.maxWidth - navWidth)
            )
        )

    val navMenuPlaceable = navMenuMeasurable.first()
        .measure(
            constraints.copy(
                minWidth = 0,
                maxWidth = padding + navWidth
            )
        )
    val dismissPlaceable = dismissMeasurable.firstOrNull()?.measure(constraints.copy(minWidth = 0, minHeight = 0))
    layout(constraints.maxWidth, constraints.maxHeight) {
        navMenuPlaceable.placeRelative(
            0,
            0
        )
        contentPlaceable.placeRelative(
            padding + navWidth,
            0
        )
        dismissPlaceable?.placeRelative(
            (padding + navWidth + contentWidth).coerceAtMost(constraints.maxWidth - dismissPlaceable.width),
            0
        )
    }
}
