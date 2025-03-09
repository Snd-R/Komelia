package io.github.snd_r.komelia.ui.color.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke.Companion.HairlineWidth
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.color.ColorChannel
import io.github.snd_r.komelia.color.HistogramPaths
import io.github.snd_r.komelia.platform.WindowSizeClass.COMPACT
import io.github.snd_r.komelia.platform.WindowSizeClass.EXPANDED
import io.github.snd_r.komelia.platform.WindowSizeClass.FULL
import io.github.snd_r.komelia.platform.WindowSizeClass.MEDIUM
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.color.LevelsState
import io.github.snd_r.komelia.ui.common.NumberFieldWithIncrements


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorLevelContent(
    state: LevelsState,
) {
    val width = LocalWindowWidth.current
    val heightModifier = remember(width) {
        when (width) {
            COMPACT, MEDIUM -> Modifier.height(600.dp)
            EXPANDED, FULL -> Modifier
        }
    }
    val channel = state.currentChannel.collectAsState().value
    val density = LocalDensity.current
    LaunchedEffect(density) { state.onDensityChange(density) }

    Column(
        modifier = heightModifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PresetsContent(
                state = state.presetsState,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
            ChannelSelection(
                selectedChannel = channel,
                onChannelChange = state::onCurveChannelChange,
                onChannelReset = state::onChannelReset,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
            OutlinedButton(
                onClick = state::onAllChannelsReset,
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .pointerHoverIcon(PointerIcon.Hand),
            ) {
                Text("Reset All")
            }
        }

        HistogramContent(
            histogramPathData = state.histogramPaths.collectAsState().value,
            selectedChannel = channel,
            onCanvasSizeChange = state::onHistogramCanvasSizeChange,
            modifier = Modifier.weight(1f)
        )
        Column {
            HorizontalGradient()
            HandleBar(state.inputHandleBarState)
            InputFields(
                lowValue = state.lowInputValue.collectAsState(0).value,
                onLowValueChange = state::onLowPointInputChange,
                highValue = state.highInputValue.collectAsState(255).value,
                onHighValueChange = state::onHighPointInputChange,
                gammaValue = state.gammaInputValue.collectAsState(1f).value,
                onGammaValueChange = state::onGammaInputChange
            )
            Text("Output Levels", modifier = Modifier.padding(vertical = 10.dp))
            HorizontalGradient()
            HandleBar(state.outputHandleBarState)
            OutputFields(
                lowValue = state.lowOutputValue.collectAsState(0).value,
                onLowValueChange = state::onLowOutputChange,
                highValue = state.highOutputValue.collectAsState(255).value,
                onHighValueChange = state::onHighOutputChange,
            )

        }

    }
}

@Composable
private fun HistogramContent(
    histogramPathData: HistogramPaths,
    selectedChannel: ColorChannel,
    onCanvasSizeChange: (IntSize) -> Unit,
    modifier: Modifier
) {
    val histogramPathOrder = remember(selectedChannel, histogramPathData) {
        histogramDrawOrder(selectedChannel, histogramPathData)
    }

    val borderColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier
            .fillMaxSize()
            .onGloballyPositioned { onCanvasSizeChange(it.size) }
    ) {
        drawRect(
            color = borderColor,
            style = Stroke(HairlineWidth),
        )

        for ((path, color) in histogramPathOrder) {
            drawPath(
                path,
                color = color,
                style = Stroke(5f * density)
            )
        }
    }
}

@Composable
private fun HorizontalGradient(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .fillMaxWidth()
            .height(16.dp)
            .border(Dp.Hairline, MaterialTheme.colorScheme.primary)
            .background(
                Brush.horizontalGradient(
                    0f to Color.Black,
                    1f to Color.White
                )
            )
    )
}


@Composable
private fun InputFields(
    lowValue: Int,
    onLowValueChange: (Int) -> Unit,
    highValue: Int,
    onHighValueChange: (Int) -> Unit,
    gammaValue: Float,
    onGammaValueChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberFieldWithIncrements(
            value = lowValue.toFloat(),
            onvValueChange = { onLowValueChange(it.toInt()) },
            label = { Text("Black") },
            stepSize = 1f,
            minValue = 0f,
            maxValue = 255f,
            digitsAfterDecimal = 0,
            modifier = Modifier.widthIn(max = 115.dp),
        )

        Spacer(Modifier.weight(1f))
        NumberFieldWithIncrements(
            value = gammaValue,
            onvValueChange = onGammaValueChange,
            label = { Text("Gamma") },
            stepSize = 0.01f,
            minValue = 0.1f,
            maxValue = 10f,
            digitsAfterDecimal = 2,
            modifier = Modifier.widthIn(max = 115.dp).padding(start = 25.dp)
        )
        Spacer(Modifier.weight(1f))
        NumberFieldWithIncrements(
            value = highValue.toFloat(),
            onvValueChange = { onHighValueChange(it.toInt()) },
            label = { Text("White") },
            stepSize = 1f,
            minValue = 0f,
            maxValue = 255f,
            digitsAfterDecimal = 0,
            modifier = Modifier.widthIn(max = 115.dp),
        )
    }
}

@Composable
private fun OutputFields(
    lowValue: Int,
    onLowValueChange: (Int) -> Unit,
    highValue: Int,
    onHighValueChange: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        NumberFieldWithIncrements(
            value = lowValue.toFloat(),
            onvValueChange = { onLowValueChange(it.toInt()) },
            label = { Text("Black") },
            stepSize = 1f,
            minValue = 0f,
            maxValue = 255f,
            digitsAfterDecimal = 0,
            modifier = Modifier.widthIn(max = 115.dp),
        )
        Spacer(Modifier.weight(1f))
        NumberFieldWithIncrements(
            value = highValue.toFloat(),
            onvValueChange = { onHighValueChange(it.toInt()) },
            label = { Text("White") },
            stepSize = 1f,
            minValue = 0f,
            maxValue = 255f,
            digitsAfterDecimal = 0,
            modifier = Modifier.widthIn(max = 115.dp),
        )
    }
}
