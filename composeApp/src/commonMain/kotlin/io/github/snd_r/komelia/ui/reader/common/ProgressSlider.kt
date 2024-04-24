package io.github.snd_r.komelia.ui.reader.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.reader.PageMetadata
import kotlin.math.roundToInt

@Composable
fun ProgressSlider(
    pages: List<PageMetadata>,
    currentPageIndex: Int,
    onPageNumberChange: (Int) -> Unit,
    show: Boolean,
    layoutDirection: LayoutDirection,
    modifier: Modifier = Modifier,
) {
    PageSpreadProgressSlider(
        pageSpreads = pages.map { listOf(it) },
        currentSpreadIndex = currentPageIndex,
        onPageNumberChange = onPageNumberChange,
        show = show,
        layoutDirection = layoutDirection,
        modifier = modifier
    )
}

@Composable
fun PageSpreadProgressSlider(
    pageSpreads: List<List<PageMetadata>>,
    currentSpreadIndex: Int,
    onPageNumberChange: (Int) -> Unit,
    show: Boolean,
    layoutDirection: LayoutDirection,
    modifier: Modifier = Modifier,
) {
    if (pageSpreads.isEmpty()) return
    val currentSpread = pageSpreads[currentSpreadIndex]
    val label = currentSpread.map { it.pageNumber }.joinToString("-")

    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState()

    Box(modifier = modifier.then(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 70.dp)
            .pointerInput(Unit) {}
            .hoverable(interactionSource)
    )) {
        if (show || isHovered.value) {
            SliderWithLabel(
                value = currentSpreadIndex.toFloat(),
                valueRange = 0f..(pageSpreads.size - 1).toFloat(),
                steps = pageSpreads.size - 2,
                direction = layoutDirection,
                onValueChange = { onPageNumberChange(it.roundToInt()) },
                label = label,
                labelMinWidth = 40.dp,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.tertiary,
                    activeTrackColor = MaterialTheme.colorScheme.tertiary,
                    activeTickColor = MaterialTheme.colorScheme.onTertiary,

                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    inactiveTickColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderWithLabel(
    value: Float,
    direction: LayoutDirection,
    label: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    colors: SliderColors = SliderDefaults.colors(),

    labelMinWidth: Dp = 40.dp,
) {
    Column(modifier) {

        CompositionLocalProvider(LocalLayoutDirection provides direction) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

                val offset = getSliderOffset(
                    value = value,
                    valueRange = valueRange,
                    boxWidth = maxWidth,
                    labelWidth = labelMinWidth + 8.dp // Since we use a padding of 4.dp on either sides of the SliderLabel, we need to account for this in our calculation
                )
                SliderLabel(
                    label = label,
                    minWidth = labelMinWidth,
                    modifier = Modifier.padding(start = offset)
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                colors = colors,
                steps = steps,
                valueRange = valueRange,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant),
                track = { state ->
                    SliderDefaults.Track(
                        sliderState = state,
                        colors = colors,
                        modifier = Modifier.scale(scaleX = 1f, scaleY = 2f),
                    )
                }
            )

        }
    }
}


@Composable
fun SliderLabel(label: String, minWidth: Dp, modifier: Modifier = Modifier) {
    Text(
        label,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.surface))
            .padding(4.dp)
            .defaultMinSize(minWidth = minWidth)
    )
}


private fun getSliderOffset(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    boxWidth: Dp,
    labelWidth: Dp
): Dp {

    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val positionFraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)

    return (boxWidth - labelWidth) * positionFraction
}


// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)
