package snd.komelia.ui.color.view

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import snd.komelia.color.normalizeFromCanvas
import snd.komelia.color.toCanvasX
import kotlin.math.sqrt

class HandleBarState(
    coroutineScope: CoroutineScope,
    normalizedPointPositions: StateFlow<List<Float>>,
    val density: StateFlow<Density>,
    private val onPositionChange: (index: Int, newValue: Float) -> Unit,
) {
    val hoverPointerIcon = MutableStateFlow(PointerIcon.Default)
    private val canvasSize = MutableStateFlow(Size.Zero)
    private val selectedPointIndex = MutableStateFlow<Int?>(null)
    private val canvasPoints = normalizedPointPositions.combine(canvasSize) { points, size ->
        points.map { it.toCanvasX(size) }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())

    val handles = combine(canvasPoints, density) { positions, density ->
        positions.mapIndexed { index, value ->
            val (color, borderColor) = when (index) {
                0 -> Color.Black to Color.White
                1 -> Color.Gray to Color.White
                2 -> Color.White to Color.Black
                else -> Color.Black to Color.White
            }

            HandlePath(
                path = trianglePath(value, density),
                color = color,
                borderColor = borderColor
            )
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())

    fun onPointerEvent(event: PointerEvent) {
        when (event.type) {
            PointerEventType.Move -> onPointerMove(event)
            PointerEventType.Press -> onPointerPress(event)
            PointerEventType.Release -> selectedPointIndex.value = null
        }
    }

    private fun onPointerMove(event: PointerEvent) {
        val position = event.changes.last().position
        val canvasPoints = canvasPoints.value

        val hitIndex = canvasPoints.indexOfFirst { pointerHitTest(position.x, it) }
        if (hitIndex != -1) {
            hoverPointerIcon.value = PointerIcon.Hand
        } else {
            hoverPointerIcon.value = PointerIcon.Default
        }
        val selectedIndex = selectedPointIndex.value ?: return
        val normalizedPosition = position.normalizeFromCanvas(1f, canvasSize.value).x.coerceIn(0f, 1f)
        onPositionChange(selectedIndex, normalizedPosition)
    }

    private fun onPointerPress(event: PointerEvent) {
        val position = event.changes.last().position
        val canvasPoints = canvasPoints.value
        val hitIndex = canvasPoints.indexOfFirst { pointerHitTest(position.x, it) }
        if (hitIndex != -1) {
            selectedPointIndex.value = hitIndex
        }
    }

    private fun pointerHitTest(pointerX: Float, targetCenter: Float): Boolean {
        val halfSize = (handleBarSize * density.value.density) / 2
        return pointerX >= targetCenter - halfSize && pointerX <= targetCenter + halfSize
    }

    fun onCanvasSizeChange(size: IntSize) {
        canvasSize.value = size.toSize()
    }

    private fun trianglePath(x: Float, density: Density): Path {
        val path = Path()
        val side = handleBarSize * density.density
        val height = side * (sqrt(3f) / 2)
        path.moveTo(x, 0f)
        path.lineTo(x + side / 2, height)
        path.lineTo(x - side / 2, height)
        path.lineTo(x, 0f)
        path.close()
        return path
    }

    data class HandlePath(
        val path: Path,
        val color: Color,
        val borderColor: Color,
    )
}


