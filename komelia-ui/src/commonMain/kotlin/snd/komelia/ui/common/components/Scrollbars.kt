package snd.komelia.ui.common.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun Modifier.scrollbar(
    state: ScrollState,
    direction: Orientation,
    indicatorThickness: Dp = 8.dp,
    indicatorColor: Color = MaterialTheme.colorScheme.background.copy(alpha = .8f),
    alpha: Float = if (state.isScrollInProgress) 0.8f else 0.5f,
    alphaAnimationSpec: AnimationSpec<Float> = tween(
        delayMillis = if (state.isScrollInProgress) 0 else 1500,
        durationMillis = if (state.isScrollInProgress) 150 else 500
    ),
    padding: PaddingValues = PaddingValues(all = 0.dp)
): Modifier = composed {
    val scrollbarAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = alphaAnimationSpec
    )

    drawWithContent {
        drawContent()

        val showScrollBar = (state.isScrollInProgress || scrollbarAlpha > 0.0f) && state.maxValue != 0

        // Draw scrollbar only if currently scrolling or if scroll animation is ongoing.
        if (showScrollBar) {
            val (topPadding, bottomPadding, startPadding, endPadding) = listOf(
                padding.calculateTopPadding().toPx(), padding.calculateBottomPadding().toPx(),
                padding.calculateStartPadding(layoutDirection).toPx(),
                padding.calculateEndPadding(layoutDirection).toPx()
            )
            val contentOffset = state.value
            val viewPortLength = if (direction == Orientation.Vertical)
                size.height else size.width
            val viewPortCrossAxisLength = if (direction == Orientation.Vertical)
                size.width else size.height
            val contentLength = max(viewPortLength + state.maxValue, 0.001f)  // To prevent divide by zero error
            val indicatorLength = ((viewPortLength / contentLength) * viewPortLength) - (
                    if (direction == Orientation.Vertical) topPadding + bottomPadding
                    else startPadding + endPadding
                    )
            val indicatorThicknessPx = indicatorThickness.toPx()

            val scrollOffsetViewPort = viewPortLength * contentOffset / contentLength

            val scrollbarSizeWithoutInsets = if (direction == Orientation.Vertical)
                Size(indicatorThicknessPx, indicatorLength)
            else Size(indicatorLength, indicatorThicknessPx)

            val scrollbarPositionWithoutInsets = if (direction == Orientation.Vertical)
                Offset(
                    x = if (layoutDirection == LayoutDirection.Ltr)
                        viewPortCrossAxisLength - indicatorThicknessPx - endPadding
                    else startPadding,
                    y = scrollOffsetViewPort + topPadding
                )
            else
                Offset(
                    x = if (layoutDirection == LayoutDirection.Ltr)
                        scrollOffsetViewPort + startPadding
                    else viewPortLength - scrollOffsetViewPort - indicatorLength - endPadding,
                    y = viewPortCrossAxisLength - indicatorThicknessPx - bottomPadding
                )

            drawRoundRect(
//                color = indicatorColor,
                color = indicatorColor,
                cornerRadius = CornerRadius(
                    x = indicatorThicknessPx / 2, y = indicatorThicknessPx / 2
                ),
                topLeft = scrollbarPositionWithoutInsets,
                size = scrollbarSizeWithoutInsets,
                alpha = scrollbarAlpha
            )
        }
    }
}

data class ScrollBarConfig(
    val indicatorThickness: Dp = 8.dp,
    val indicatorColor: Color = Color.LightGray,
    val alpha: Float? = null,
    val alphaAnimationSpec: AnimationSpec<Float>? = null,
    val padding: PaddingValues = PaddingValues(all = 0.dp)
)

@Composable
fun Modifier.verticalScrollWithScrollbar(
    state: ScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false,
    scrollbarConfig: ScrollBarConfig = ScrollBarConfig()
) = this
    .scrollbar(
        state, Orientation.Vertical,
        indicatorThickness = scrollbarConfig.indicatorThickness,
        indicatorColor = scrollbarConfig.indicatorColor,
        alpha = scrollbarConfig.alpha ?: if (state.isScrollInProgress) 0.8f else 0f,
        alphaAnimationSpec = scrollbarConfig.alphaAnimationSpec ?: tween(
            delayMillis = if (state.isScrollInProgress) 0 else 1500,
            durationMillis = if (state.isScrollInProgress) 150 else 500
        ),
        padding = scrollbarConfig.padding
    )
    .verticalScroll(state, enabled, flingBehavior, reverseScrolling)


@Composable
fun Modifier.horizontalScrollWithScrollbar(
    state: ScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false,
    scrollbarConfig: ScrollBarConfig = ScrollBarConfig()
) = this
    .scrollbar(
        state, Orientation.Horizontal,
        indicatorThickness = scrollbarConfig.indicatorThickness,
        indicatorColor = scrollbarConfig.indicatorColor,
        alpha = scrollbarConfig.alpha ?: if (state.isScrollInProgress) 0.8f else 0f,
        alphaAnimationSpec = scrollbarConfig.alphaAnimationSpec ?: tween(
            delayMillis = if (state.isScrollInProgress) 0 else 1500,
            durationMillis = if (state.isScrollInProgress) 150 else 500
        ),
        padding = scrollbarConfig.padding
    )
    .horizontalScroll(state, enabled, flingBehavior, reverseScrolling)
