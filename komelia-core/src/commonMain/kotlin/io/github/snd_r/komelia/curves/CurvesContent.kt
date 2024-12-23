package io.github.snd_r.komelia.curves

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke.Companion.HairlineWidth
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.curves.Curve.CurvePoint
import io.github.snd_r.komelia.curves.Curve.CurvePointType
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry

@Composable
fun CurvesContent(
    points: List<CurvePoint>,
    referenceLine: Path,
    selectedChannel: CurveColorChannel,
    availableChannels: List<CurveColorChannel>,
    onChannelChange: (CurveColorChannel) -> Unit,
    onPointsReset: () -> Unit,
    colorCurve: Path,
    redCurve: Path,
    greenCurve: Path,
    blueCurve: Path,
    histogram: Histogram,
    selectedPoint: SelectedPoint?,
    pointType: CurvePointType,
    onPointTypeChange: (CurvePointType) -> Unit,
    pointerIcon: PointerIcon,
    onPointerEvent: (PointerEvent) -> Unit,
    onCanvasSizeChange: (IntSize) -> Unit,
    onDensityChange: (Density) -> Unit,
    displayImage: ImageBitmap?,
    onMaxHeightChange: (Int) -> Unit,
    pointerPosition: Offset,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CurveContent(
            points = points,
            referenceLine = referenceLine,
            selectedChannel = selectedChannel,
            availableChannels = availableChannels,
            onChannelChange = onChannelChange,
            onPointsReset = onPointsReset,
            colorCurve = colorCurve,
            redCurve = redCurve,
            greenCurve = greenCurve,
            blueCurve = blueCurve,
            histogram = histogram,
            selectedPoint = selectedPoint,
            pointType = pointType,
            onPointTypeChange = onPointTypeChange,
            pointerIcon = pointerIcon,
            pointerPosition = pointerPosition,
            onPointerEvent = onPointerEvent,
            onCanvasSizeChange = onCanvasSizeChange,
            onDensityChange = onDensityChange,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .onGloballyPositioned { onMaxHeightChange(it.size.height) },
            contentAlignment = Alignment.Center
        ) {
            if (displayImage != null) {
                Image(
                    displayImage,
                    null,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CurveContent(
    points: List<CurvePoint>,
    referenceLine: Path,
    selectedChannel: CurveColorChannel,
    availableChannels: List<CurveColorChannel>,
    onChannelChange: (CurveColorChannel) -> Unit,
    onPointsReset: () -> Unit,
    colorCurve: Path,
    redCurve: Path,
    greenCurve: Path,
    blueCurve: Path,
    histogram: Histogram,
    selectedPoint: SelectedPoint?,
    pointType: CurvePointType,
    onPointTypeChange: (CurvePointType) -> Unit,
    pointerIcon: PointerIcon,
    onPointerEvent: (PointerEvent) -> Unit,
    onCanvasSizeChange: (IntSize) -> Unit,
    onDensityChange: (Density) -> Unit,
    pointerPosition: Offset,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Channel:")
                DropdownChoiceMenu(
                    selectedOption = remember(selectedChannel) { LabeledEntry(selectedChannel, selectedChannel.name) },
                    options = remember(availableChannels) { availableChannels.map { LabeledEntry(it, it.name) } },
                    onOptionChange = { onChannelChange(it.value) },
                    inputFieldModifier = Modifier.widthIn(min = 150.dp)
                )
                ElevatedButton(
                    onClick = onPointsReset,
                    shape = RoundedCornerShape(5.dp)
                ) { Text("Reset Channel") }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Point Type:")
                val primaryColor = MaterialTheme.colorScheme.primary
                val selectColor = MaterialTheme.colorScheme.surfaceVariant
                Box(
                    Modifier
                        .clip(RoundedCornerShape(5.dp))
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

        if (pointerPosition.isUnspecified) Text("No coordinates")
        else Text("x: ${pointerPosition.x} y: ${pointerPosition.y}")

        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VerticaGradient(Modifier.padding(bottom = 30.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Curve(
                    points = points,
                    referenceLine = referenceLine,
                    selectedChannel = selectedChannel,
                    colorCurve = colorCurve,
                    redCurve = redCurve,
                    greenCurve = greenCurve,
                    blueCurve = blueCurve,
                    histogram = histogram,
                    selectedPoint = selectedPoint,
                    pointerIcon = pointerIcon,
                    onPointerEvent = onPointerEvent,
                    onCanvasSizeChange = onCanvasSizeChange,
                    onDensityChange = onDensityChange,
                    modifier = Modifier.weight(1f)
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
            .border(Dp.Hairline, Color.Black)
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
            .border(Dp.Hairline, Color.Black)
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
    points: List<CurvePoint>,
    referenceLine: Path,
    selectedChannel: CurveColorChannel,
    colorCurve: Path,
    redCurve: Path,
    greenCurve: Path,
    blueCurve: Path,
    histogram: Histogram,
    selectedPoint: SelectedPoint?,
    pointerIcon: PointerIcon,
    onPointerEvent: (PointerEvent) -> Unit,
    onCanvasSizeChange: (IntSize) -> Unit,
    onDensityChange: (Density) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorCurveColor = remember(selectedChannel) {
        if (selectedChannel == CurveColorChannel.VALUE) Color.Gray else Color.Gray.copy(alpha = 0.5f)
    }

    val redCurveColor = remember(selectedChannel) {
        if (selectedChannel == CurveColorChannel.RED) Color.Red else Color.Red.copy(alpha = 0.5f)
    }
    val greenCurveColor = remember(selectedChannel) {
        if (selectedChannel == CurveColorChannel.GREEN) Color.Green else Color.Green.copy(alpha = 0.5f)
    }
    val blueCurveColor = remember(selectedChannel) {
        if (selectedChannel == CurveColorChannel.BLUE) Color.Blue else Color.Blue.copy(alpha = 0.5f)
    }
    val currentDensity = LocalDensity.current
    LaunchedEffect(Unit) { onDensityChange(currentDensity) }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerHoverIcon(pointerIcon)
            .pointerInput(PointerEventPass.Main) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        onPointerEvent(event)
                        event.changes.forEach { it.consume() }
                    }
                }
            }
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .onGloballyPositioned { onCanvasSizeChange(it.size) }
    ) {
        drawRect(
            topLeft = Offset(-4f, -4f).times(density),
            color = Color.Black,
            style = Stroke(HairlineWidth),
            size = Size(width = size.width + 4f * density, height = size.height + 4 * density),
        )
        drawPath(
            histogram.getCanvasPath(size),
            color = Color.Gray.copy(alpha = 0.5f),
            style = Stroke(5f * density)
        )
        drawPath(
            path = referenceLine,
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
                path = colorCurve,
                color = colorCurveColor,
                style = Stroke(3f)
            )
            drawPath(
                path = redCurve,
                color = redCurveColor,
                style = Stroke(3f)
            )
            drawPath(
                path = greenCurve,
                color = greenCurveColor,
                style = Stroke(3f)
            )
            drawPath(
                path = blueCurve,
                color = blueCurveColor,
                style = Stroke(3f)
            )
        }

        val pointSize = pointSize * density
        for ((index, point) in points.withIndex()) {
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