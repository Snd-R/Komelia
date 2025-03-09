package io.github.snd_r.komelia.ui.color.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke.Companion.HairlineWidth
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.color.ColorChannel
import io.github.snd_r.komelia.color.CurvePointType
import io.github.snd_r.komelia.color.HistogramPaths
import io.github.snd_r.komelia.platform.WindowSizeClass.COMPACT
import io.github.snd_r.komelia.platform.WindowSizeClass.EXPANDED
import io.github.snd_r.komelia.platform.WindowSizeClass.FULL
import io.github.snd_r.komelia.platform.WindowSizeClass.MEDIUM
import io.github.snd_r.komelia.ui.LocalWindowWidth
import io.github.snd_r.komelia.ui.color.CurveDrawData
import io.github.snd_r.komelia.ui.color.CurvePresetsState
import io.github.snd_r.komelia.ui.color.SelectedPoint
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.NumberFieldWithIncrements
import kotlin.math.roundToInt

const val curvePointSize = 10f

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorCurvesContent(
    curvePathData: CurveDrawData,
    histogramPathData: HistogramPaths,

    selectedChannel: ColorChannel,
    onChannelChange: (ColorChannel) -> Unit,
    onChannelReset: () -> Unit,
    onAllChannelsReset: () -> Unit,

    selectedPoint: SelectedPoint?,
    currentPointOffset: IntOffset?,
    onPointChange: (SelectedPoint, newOffset: IntOffset) -> Unit,

    pointType: CurvePointType,
    onPointTypeChange: (CurvePointType) -> Unit,
    pointerIcon: PointerIcon,
    onKeyEvent: (KeyEvent) -> Unit,
    onPointerEvent: (PointerEvent) -> Unit,
    onCanvasSizeChange: (IntSize) -> Unit,
    onDensityChange: (Density) -> Unit,
    curvePointerPosition: Offset,
    presetsState: CurvePresetsState,
) {
    val width = LocalWindowWidth.current
    val heightModifier = remember(width) {
        when (width) {
            COMPACT, MEDIUM -> Modifier.height(600.dp)
            EXPANDED, FULL -> Modifier
        }
    }
    Column(
        modifier = heightModifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PresetsContent(
                state = presetsState,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            PointTypeSelection(
                pointType = pointType,
                onPointTypeChange = onPointTypeChange,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            ChannelValues(
                selectedPoint = selectedPoint,
                currentPointOffset = currentPointOffset,
                onPointChange = onPointChange,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            ChannelSelection(
                selectedChannel = selectedChannel,
                onChannelChange = onChannelChange,
                onChannelReset = onChannelReset,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            OutlinedButton(
                onClick = onAllChannelsReset,
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .pointerHoverIcon(PointerIcon.Hand),
            ) {
                Text("Reset All")
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VerticaGradient(Modifier.padding(bottom = 30.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Curve(
                    curvePathData = curvePathData,
                    histogramPathData = histogramPathData,
                    curvePointerPosition = curvePointerPosition,
                    selectedChannel = selectedChannel,
                    selectedPoint = selectedPoint,
                    pointerIcon = pointerIcon,
                    onKeyEvent = onKeyEvent,
                    onPointerEvent = onPointerEvent,
                    onCanvasSizeChange = onCanvasSizeChange,
                    onDensityChange = onDensityChange,
                )
                HorizontalGradient()
            }
        }
    }

}

@Composable
private fun VerticaGradient(modifier: Modifier = Modifier) {
    Spacer(
        modifier
            .fillMaxHeight()
            .width(16.dp)
            .border(Dp.Hairline, MaterialTheme.colorScheme.primary)
            .background(
                Brush.verticalGradient(
                    0f to Color.White,
                    1f to Color.Black
                )
            )
    )

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
private fun Curve(
    curvePathData: CurveDrawData,
    histogramPathData: HistogramPaths,
    curvePointerPosition: Offset,
    selectedChannel: ColorChannel,
    selectedPoint: SelectedPoint?,
    pointerIcon: PointerIcon,
    onKeyEvent: (KeyEvent) -> Unit,
    onPointerEvent: (PointerEvent) -> Unit,
    onCanvasSizeChange: (IntSize) -> Unit,
    onDensityChange: (Density) -> Unit,
) {
    val colorCurveColor = remember(selectedChannel) {
        if (selectedChannel == ColorChannel.VALUE) Color.Gray else Color.Gray.copy(alpha = 0.5f)
    }

    val redCurveColor = remember(selectedChannel) {
        if (selectedChannel == ColorChannel.RED) Color.Red else Color.Red.copy(alpha = 0.5f)
    }
    val greenCurveColor = remember(selectedChannel) {
        if (selectedChannel == ColorChannel.GREEN) Color.Green else Color.Green.copy(alpha = 0.5f)
    }
    val blueCurveColor = remember(selectedChannel) {
        if (selectedChannel == ColorChannel.BLUE) Color.Blue else Color.Blue.copy(alpha = 0.5f)
    }
    val currentDensity = LocalDensity.current
    LaunchedEffect(Unit) { onDensityChange(currentDensity) }
    val histogramPathOrder = remember(histogramPathData, selectedChannel) {
        histogramDrawOrder(selectedChannel, histogramPathData)
    }


    val textMeasurer = rememberTextMeasurer()
    val textLayout = remember(curvePointerPosition) {
        if (curvePointerPosition.isUnspecified) null
        else {
            val text = "x: ${curvePointerPosition.x.roundToInt()} y: ${curvePointerPosition.y.roundToInt()}"
            textMeasurer.measure(text)
        }
    }

    val textBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.primary
    val focusRequester = remember { FocusRequester() }
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent {
                onKeyEvent(it)
                true
            }
            .pointerHoverIcon(pointerIcon)
            .pointerInput(PointerEventPass.Main) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        if (event.type == PointerEventType.Press) {
                            focusRequester.requestFocus()
                        }
                        onPointerEvent(event)
                        event.changes.forEach { it.consume() }
                    }
                }
            }
            .onGloballyPositioned { onCanvasSizeChange(it.size) }
    ) {
        drawRect(
            topLeft = Offset(-4f, -4f).times(density),
            color = borderColor,
            style = Stroke(HairlineWidth),
            size = Size(width = size.width + 4f * density, height = size.height + 4 * density),
        )
        if (textLayout != null) {
            val backgroundTopLeft = 5 * density
            val backgroundSize = Size(
                textLayout.size.width.toFloat() + 20 * density,
                textLayout.size.height + 20 * density,
            )
            drawRect(
                color = textBackgroundColor.copy(alpha = 0.6f),
                topLeft = Offset(backgroundTopLeft, backgroundTopLeft),
                size = backgroundSize
            )
            drawText(
                textLayoutResult = textLayout,
                color = textColor,
                topLeft = Offset(
                    backgroundTopLeft + (10 * density),
                    backgroundTopLeft + (10 * density)
                )
            )
        }
        for ((path, color) in histogramPathOrder) {
            drawPath(
                path,
                color = color,
                style = Stroke(5f * density)
            )
        }

        drawPath(
            path = curvePathData.referenceLine,
            color = Color.Gray.copy(alpha = 0.3f),
            style = Stroke(1f * density)
        )
        clipRect(
            -3f * density,
            -3f * density,
            size.width + (3 * density),
            size.height + (3 * density)
        ) {
            drawPath(
                path = curvePathData.colorCurve,
                color = colorCurveColor,
                style = Stroke(3f)
            )
            drawPath(
                path = curvePathData.redCurve,
                color = redCurveColor,
                style = Stroke(3f)
            )
            drawPath(
                path = curvePathData.greenCurve,
                color = greenCurveColor,
                style = Stroke(3f)
            )
            drawPath(
                path = curvePathData.blueCurve,
                color = blueCurveColor,
                style = Stroke(3f)
            )

        }

        val pointSize = curvePointSize * density
        for ((index, point) in curvePathData.points.withIndex()) {
            val color = if (selectedPoint != null && !selectedPoint.isRemoved && index == selectedPoint.index)
                Color.Green else Color.DarkGray
            when (point.type) {
                CurvePointType.SMOOTH -> drawCircle(color, pointSize, point.toOffset())
                CurvePointType.CORNER -> {
                    rotate(45f, point.toOffset()) {
                        drawRect(
                            color = color,
                            topLeft = Offset(point.x - pointSize, point.y - pointSize),
                            size = Size(pointSize * 2, pointSize * 2)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelSelection(
    selectedChannel: ColorChannel,
    onChannelChange: (ColorChannel) -> Unit,
    onChannelReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        DropdownChoiceMenu(
            selectedOption = remember(selectedChannel) { LabeledEntry(selectedChannel, selectedChannel.name) },
            options = remember { ColorChannel.entries.map { LabeledEntry(it, it.name) } },
            label = { Text("Channel") },
            onOptionChange = { onChannelChange(it.value) },
            inputFieldModifier = Modifier.widthIn(min = 150.dp)

        )
        Tooltip("Reset Channel") {
            IconButton(onClick = onChannelReset) {
                Icon(Icons.Default.SettingsBackupRestore, null)
            }
        }
    }

}

@Composable
private fun PointTypeSelection(
    pointType: CurvePointType,
    onPointTypeChange: (CurvePointType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text("Point Type", style = MaterialTheme.typography.labelMedium)
        val primaryColor = MaterialTheme.colorScheme.primary
        val selectColor = MaterialTheme.colorScheme.surfaceVariant
        Row {
            Box(
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .border(Dp.Hairline, selectColor)
                    .background(if (pointType == CurvePointType.SMOOTH) selectColor else Color.Unspecified)
                    .clickable { onPointTypeChange(CurvePointType.SMOOTH) }
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Smooth")
                    Canvas(Modifier.size(28.dp).padding(6.dp)) { drawCircle(primaryColor) }
                }
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .border(Dp.Hairline, selectColor)
                    .background(if (pointType == CurvePointType.CORNER) selectColor else Color.Unspecified)
                    .clickable { onPointTypeChange(CurvePointType.CORNER) }
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Corner")
                    Canvas(Modifier.size(28.dp).padding(6.dp)) { rotate(45f) { drawRect(primaryColor) } }
                }
            }
        }
    }

}

@Composable
private fun ChannelValues(
    selectedPoint: SelectedPoint?,
    currentPointOffset: IntOffset?,
    onPointChange: (SelectedPoint, newOffset: IntOffset) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        NumberFieldWithIncrements(
            value = currentPointOffset?.x?.toFloat(),
            onvValueChange = { newX ->
                selectedPoint?.let { onPointChange(selectedPoint, IntOffset(newX.toInt(), currentPointOffset?.y ?: 0)) }
            },
            label = { Text("Input") },
            stepSize = 1f,
            minValue = 0f,
            maxValue = 255f,
            digitsAfterDecimal = 0,
            modifier = Modifier.widthIn(max = 115.dp)
        )
        NumberFieldWithIncrements(
            value = currentPointOffset?.y?.toFloat(),
            onvValueChange = { newY ->
                selectedPoint?.let { onPointChange(selectedPoint, IntOffset(currentPointOffset?.x ?: 0, newY.toInt())) }
            },
            label = { Text("Output") },
            stepSize = 1f,
            minValue = 0f,
            maxValue = 255f,
            digitsAfterDecimal = 0,
            modifier = Modifier.widthIn(max = 115.dp)
        )
    }
}
