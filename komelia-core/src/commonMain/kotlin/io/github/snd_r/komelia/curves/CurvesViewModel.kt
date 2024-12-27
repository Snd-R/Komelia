package io.github.snd_r.komelia.curves

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import cafe.adriel.voyager.core.model.ScreenModel
import io.github.snd_r.komelia.curves.Curve.CurvePoint
import io.github.snd_r.komelia.curves.Curve.CurvePointType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import snd.komelia.image.KomeliaImage
import kotlin.math.abs

const val pointSize = 7f

data class SelectedPoint(
    val index: Int,
    val isMoving: Boolean,
    val isRemoved: Boolean,
)

enum class CurveColorChannel {
    VALUE,
    RED,
    GREEN,
    BLUE
}

class CurvesViewModel : ScreenModel {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val originalImage = getImage()
    private val canvasSize = MutableStateFlow(Size.Zero)
    private val density = MutableStateFlow(Density(1f, 1f))
    private val imageMaxHeight = MutableStateFlow<Int?>(null)
    private val colorCurve = Curve()
    private val redCurve = Curve()
    private val greenCurve = Curve()
    private val blueCurve = Curve()

    val availableChannels = when (originalImage.bands) {
        1 -> listOf(CurveColorChannel.VALUE)
        else -> listOf(CurveColorChannel.VALUE, CurveColorChannel.RED, CurveColorChannel.GREEN, CurveColorChannel.BLUE)
    }
    val currentChannel = MutableStateFlow(CurveColorChannel.VALUE)
    val histogram = getHistogram(originalImage)
    val pointerIcon = MutableStateFlow(PointerIcon.Crosshair)
    val pointerCoordinates = MutableStateFlow(Offset.Unspecified)
    val selectedPoint = MutableStateFlow<SelectedPoint?>(null)
    val pointType = MutableStateFlow(CurvePointType.SMOOTH)

    val colorCurvePath = canvasSize.combine(colorCurve.path) { size, path -> path.denormalize(size) }
        .stateIn(coroutineScope, SharingStarted.Lazily, Path())
    val redCurvePath = canvasSize.combine(redCurve.path) { size, path -> path.denormalize(size) }
        .stateIn(coroutineScope, SharingStarted.Lazily, Path())
    val greenCurvePath = canvasSize.combine(greenCurve.path) { size, path -> path.denormalize(size) }
        .stateIn(coroutineScope, SharingStarted.Lazily, Path())
    val blueCurvePath = canvasSize.combine(blueCurve.path) { size, path -> path.denormalize(size) }
        .stateIn(coroutineScope, SharingStarted.Lazily, Path())

    private val selectedCurve = currentChannel.map {
        when (it) {
            CurveColorChannel.VALUE -> colorCurve
            CurveColorChannel.RED -> redCurve
            CurveColorChannel.GREEN -> greenCurve
            CurveColorChannel.BLUE -> blueCurve
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, colorCurve)

    @OptIn(ExperimentalCoroutinesApi::class)
    val controlPoints = selectedCurve
        .flatMapLatest { it.points }
        .combine(canvasSize) { points, canvasSize -> points.map { it.denormalize(canvasSize) } }
        .stateIn(coroutineScope, SharingStarted.Lazily, emptyList())

    val referenceLine = canvasSize.map { size ->
        val path = Path()
        val start = Offset(0f, 0f).denormalize(size)
        val end = Offset(1f, 1f).denormalize(size)
        path.moveTo(start.x, start.y)
        path.lineTo(end.x, end.y)
        path
    }.stateIn(coroutineScope, SharingStarted.Lazily, Path())

    private val lookupTables = combine(
        colorCurve.lookupTable,
        redCurve.lookupTable,
        greenCurve.lookupTable,
        blueCurve.lookupTable
    ) { all, red, green, blue ->
        listOf(
            all,
            red,
            green,
            blue
        )
    }

    val displayImage = imageMaxHeight.filterNotNull()
        .combine(lookupTables) { height, lut -> height to lut }
        .conflate()
        .map { (height, lut) ->
            val resized = transformImage(
                originalImage,
                height,
                lut[0],
                lut[1],
                lut[2],
                lut[3]
            )
            val imageBitmap = toImageBitmap(resized)
            imageBitmap
        }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    fun onPointerEvent(event: PointerEvent) {
        when (event.type) {
            PointerEventType.Move -> onPointerMove(event)
            PointerEventType.Exit -> pointerCoordinates.value = Offset.Unspecified
            PointerEventType.Press -> onPointerPress(event)
            PointerEventType.Release -> onPointerRelease()
        }
    }

    private fun onPointerMove(event: PointerEvent) {
        val position = event.changes.last().position
        val normalizedPosition = position.normalize(canvasSize.value)
        pointerCoordinates.value = Offset(
            x = normalizedPosition.x,
            y = normalizedPosition.y
        )

        val selectedPoint = selectedPoint.value

        if (selectedPoint != null && selectedPoint.isMoving) {
            val curve = selectedCurve.value
            val curvePoints = curve.points.value

            val isBeforePrevious = curvePoints.getOrNull(selectedPoint.index - 1)
                ?.let { normalizedPosition.x <= it.x } ?: false

            val isAfterNext = curvePoints
                .getOrNull(if (selectedPoint.isRemoved) selectedPoint.index else selectedPoint.index + 1)
                ?.let { normalizedPosition.x >= it.x } ?: false
            val isInvalidPoint = isBeforePrevious || isAfterNext

            if (isInvalidPoint) {
                if (!selectedPoint.isRemoved) {
                    this.selectedPoint.update { (it ?: selectedPoint).copy(isRemoved = true) }
                    curve.removePoint(selectedPoint.index)
                }
            } else {
                if (selectedPoint.isRemoved) {
                    curve.addPoint(selectedPoint.index, CurvePoint(normalizedPosition, pointType.value))
                    this.selectedPoint.update { (it ?: selectedPoint).copy(isRemoved = false) }
                } else {
                    curve.updatePoint(selectedPoint.index, CurvePoint(normalizedPosition, pointType.value))
                }
            }
        }
        val density = density.value.density
        if (controlPoints.value.any { Rect(it.toOffset(), pointSize * density * 1.3f).contains(position) }) {
            pointerIcon.value = PointerIcon.Hand
        } else {
            pointerIcon.value = PointerIcon.Crosshair
        }
    }

    private fun onPointerPress(event: PointerEvent) {
        val position = event.changes.last().position
        val normalizedPosition = position.normalize(canvasSize.value)
        val density = density.value.density
        val selectedIndex = controlPoints.value.indexOfFirst {
            Rect(it.toOffset(), pointSize * density * 1.3f).contains(position)
        }
        if (selectedIndex != -1) {
            selectedPoint.value = SelectedPoint(index = selectedIndex, isMoving = true, isRemoved = false)
            val curvePoint = selectedCurve.value.getPoint(selectedIndex)
            pointType.value = curvePoint?.type ?: CurvePointType.SMOOTH
        }

        val curve = selectedCurve.value
        val curvePoints = curve.points.value
        if (selectedIndex == -1) {
            val insertionIndex = curvePoints.indexOfFirst { it.x > normalizedPosition.x }
            if (insertionIndex == -1) {
                curve.addPoint(CurvePoint(normalizedPosition, pointType.value))
                selectedPoint.value = SelectedPoint(curvePoints.size, isMoving = true, isRemoved = false)
            } else {
                curve.addPoint(insertionIndex, CurvePoint(normalizedPosition, pointType.value))
                selectedPoint.value = SelectedPoint(insertionIndex, isMoving = true, isRemoved = false)
            }
        }
    }

    private fun onPointerRelease() {
        selectedPoint.update {
            if (it == null || it.isRemoved) null
            else it.copy(isMoving = false)
        }
    }

    fun onCanvasSizeChange(size: IntSize) {
        canvasSize.value = size.toSize()
    }

    fun onDensityChange(density: Density) {
        this.density.value = density
    }

    fun onImageMaxHeightChange(height: Int) {
        imageMaxHeight.value = height
    }

    fun onCurveChannelChange(channel: CurveColorChannel) {
        selectedPoint.value = null
        currentChannel.value = channel
    }

    fun onPointsReset() {
        when (currentChannel.value) {
            CurveColorChannel.VALUE -> colorCurve.resetPoints()
            CurveColorChannel.RED -> redCurve.resetPoints()
            CurveColorChannel.GREEN -> greenCurve.resetPoints()
            CurveColorChannel.BLUE -> blueCurve.resetPoints()
        }
    }

    fun onPointTypeChange(type: CurvePointType) {
        pointType.value = type
        selectedPoint.value?.let { selectedCurve.value.updatePointType(it.index, type) }
    }

    data class NormalizationRatio(val x: Float, val y: Float)

    private fun calculateNormalisationRatio(canvasSize: IntSize) =
        NormalizationRatio(1f / canvasSize.width, 1f / canvasSize.height)

    private fun Float.toCanvasX(normalizationRatio: NormalizationRatio) = this / normalizationRatio.x
    private fun Float.toCanvasY(normalizationRatio: NormalizationRatio) = abs(this - 1f) / normalizationRatio.y
    private fun Offset.toCanvasCoordinates(normalizationRatio: NormalizationRatio) =
        Offset(this.x.toCanvasX(normalizationRatio), this.y.toCanvasY(normalizationRatio))

    private fun Float.toNormalizedX(normalizationRatio: NormalizationRatio) =
        this.coerceIn(0f, canvasSize.value.width.toFloat()) * normalizationRatio.x

    private fun Float.toNormalizedY(normalizationRatio: NormalizationRatio) =
        abs(this.coerceIn(0f, canvasSize.value.height.toFloat()) - canvasSize.value.height) * normalizationRatio.y

    private fun Offset.toNormalizedCoordinates(normalizationRatio: NormalizationRatio) =
        Offset(this.x.toNormalizedX(normalizationRatio), this.y.toNormalizedY(normalizationRatio))
}

expect fun getImage(): KomeliaImage
expect fun transformImage(
    image: KomeliaImage,
    targetHeight: Int,
    colorLut: LookupTable?,
    redLut: LookupTable?,
    greenLut: LookupTable?,
    blueLut: LookupTable?,
): KomeliaImage

expect fun toImageBitmap(image: KomeliaImage): ImageBitmap
expect fun getHistogram(image: KomeliaImage): Histogram