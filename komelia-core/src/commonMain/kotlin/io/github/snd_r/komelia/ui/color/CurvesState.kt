package io.github.snd_r.komelia.ui.color

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.color.ColorChannel
import io.github.snd_r.komelia.color.ColorCurvePoints
import io.github.snd_r.komelia.color.Curve
import io.github.snd_r.komelia.color.CurvePoint
import io.github.snd_r.komelia.color.CurvePointType
import io.github.snd_r.komelia.color.Histogram
import io.github.snd_r.komelia.color.HistogramPaths
import io.github.snd_r.komelia.color.RGBA8888LookupTable
import io.github.snd_r.komelia.color.denormalizeToCanvas
import io.github.snd_r.komelia.color.identityMap
import io.github.snd_r.komelia.color.normalizeFromCanvas
import io.github.snd_r.komelia.color.repository.BookColorCorrectionRepository
import io.github.snd_r.komelia.color.repository.ColorCurvePresetRepository
import io.github.snd_r.komelia.ui.color.view.curvePointSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import snd.komga.client.book.KomgaBookId
import kotlin.math.roundToInt


class CurvesState(
    appNotifications: AppNotifications,
    curvePresetRepository: ColorCurvePresetRepository,
    histogram: StateFlow<Histogram>,
    coroutineScope: CoroutineScope,
    private val bookId: KomgaBookId,
    private val bookCurvesRepository: BookColorCorrectionRepository,
) {
    private val canvasSize = MutableStateFlow(Size.Zero)

    @OptIn(ExperimentalCoroutinesApi::class)
    val histogramPaths = histogram.flatMapLatest { it.getDrawPathFlow(canvasSize) }
        .stateIn(coroutineScope, SharingStarted.Lazily, HistogramPaths(null, null, null, null))

    private val density = MutableStateFlow(Density(1f, 1f))

    val colorCurve = Curve()
    val redCurve = Curve()
    val greenCurve = Curve()
    val blueCurve = Curve()

    val rgbaLut = combine(
        redCurve.lookupTable,
        greenCurve.lookupTable,
        blueCurve.lookupTable,
    ) { red, green, blue ->
        if (red == null && green == null && blue == null) null
        else RGBA8888LookupTable(
            red = red ?: identityMap,
            green = green ?: identityMap,
            blue = blue ?: identityMap,
            alpha = identityMap
        )
    }

    val currentChannel = MutableStateFlow(ColorChannel.VALUE)
    val pointerIcon = MutableStateFlow(PointerIcon.Crosshair)
    val displayPointerCoordinates = MutableStateFlow(Offset.Unspecified)
    val selectedPoint = MutableStateFlow<SelectedPoint?>(null)
    val pointType = MutableStateFlow(CurvePointType.SMOOTH)

    private val points = combine(
        colorCurve.points,
        redCurve.points,
        greenCurve.points,
        blueCurve.points,
    ) { color, red, green, blue -> ColorCurvePoints(color, red, green, blue) }
        .stateIn(coroutineScope, SharingStarted.Eagerly, ColorCurvePoints.DEFAULT)


    val presetsState = CurvePresetsState(
        presetRepository = curvePresetRepository,
        appNotifications = appNotifications,
        points = points,
        coroutineScope = coroutineScope,
        onPointsChange = { points ->
            colorCurve.setPoints(points.colorCurvePoints)
            redCurve.setPoints(points.redCurvePoints)
            greenCurve.setPoints(points.greenCurvePoints)
            blueCurve.setPoints(points.blueCurvePoints)
        },
    )

    private val selectedCurve = currentChannel.map {
        when (it) {
            ColorChannel.VALUE -> colorCurve
            ColorChannel.RED -> redCurve
            ColorChannel.GREEN -> greenCurve
            ColorChannel.BLUE -> blueCurve
        }
    }.stateIn(coroutineScope, SharingStarted.Eagerly, colorCurve)

    @OptIn(ExperimentalCoroutinesApi::class)
    val controlPoints = selectedCurve
        .flatMapLatest { it.points }
        .combine(canvasSize) { points, canvasSize -> points.map { it.denormalizeToCanvas(canvasSize) } }
        .stateIn(coroutineScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedPointOffset255 = selectedCurve.flatMapLatest { it.points }
        .combine(selectedPoint) { points, selected -> selected?.let { points.getOrNull(it.index) } }
        .map { point -> point?.let { IntOffset((it.x * 255).roundToInt(), (it.y * 255).roundToInt()) } }
        .stateIn(coroutineScope, SharingStarted.Lazily, null)

    private val referenceLine = canvasSize.map { size ->
        val path = Path()
        val start = Offset(0f, 0f).denormalizeToCanvas(size)
        val end = Offset(1f, 1f).denormalizeToCanvas(size)
        path.moveTo(start.x, start.y)
        path.lineTo(end.x, end.y)
        path
    }.stateIn(coroutineScope, SharingStarted.Lazily, Path())

    @Suppress("UNCHECKED_CAST")
    val curvePathData = combine(
        referenceLine,
        canvasSize.combine(colorCurve.path) { size, path -> path.denormalizeToCanvas(size) },
        canvasSize.combine(redCurve.path) { size, path -> path.denormalizeToCanvas(size) },
        canvasSize.combine(greenCurve.path) { size, path -> path.denormalizeToCanvas(size) },
        canvasSize.combine(blueCurve.path) { size, path -> path.denormalizeToCanvas(size) },
        controlPoints,
    ) { any ->
        CurveDrawData(
            referenceLine = any[0] as Path,
            colorCurve = any[1] as Path,
            redCurve = any[2] as Path,
            greenCurve = any[3] as Path,
            blueCurve = any[4] as Path,
            points = any[5] as List<CurvePoint>
        )
    }.stateIn(coroutineScope, SharingStarted.Eagerly, CurveDrawData.EMPTY)

    suspend fun initialize() {
        bookCurvesRepository.getCurve(bookId).first()?.let { points ->
            val channels = points.channels
            colorCurve.setPoints(channels.colorCurvePoints)
            redCurve.setPoints(channels.redCurvePoints)
            greenCurve.setPoints(channels.greenCurvePoints)
            blueCurve.setPoints(channels.blueCurvePoints)
        }

        presetsState.initialize()
    }

    fun onKeyEvent(event: KeyEvent) {
        if (event.type == KeyEventType.KeyUp && event.key == Key.Delete) {
            val curve = selectedCurve.value
            val selectedPoint = this.selectedPoint.value
            if (selectedPoint != null) {
                curve.removePoint(selectedPoint.index)
                this.selectedPoint.value = null
            }
        }
    }

    fun onPointerEvent(event: PointerEvent) {
        when (event.type) {
            PointerEventType.Move -> onPointerMove(event)
            PointerEventType.Exit -> displayPointerCoordinates.value = Offset.Unspecified
            PointerEventType.Press -> onPointerPress(event)
            PointerEventType.Release -> onPointerRelease()
        }
    }

    private fun onPointerMove(event: PointerEvent) {
        val position = event.changes.last().position
        displayPointerCoordinates.value = position.normalizeFromCanvas(255f, canvasSize.value)

        val normalizedPosition = position.normalizeFromCanvas(1f, canvasSize.value)
        val selectedPoint = selectedPoint.value

        if (selectedPoint != null && selectedPoint.isMoving) {
            presetsState.deselectCurrent()
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
        if (controlPoints.value.any { Rect(it.toOffset(), curvePointSize * density * 1.3f).contains(position) }) {
            pointerIcon.value = PointerIcon.Hand
        } else {
            pointerIcon.value = PointerIcon.Crosshair
        }
    }

    private fun onPointerPress(event: PointerEvent) {
        val position = event.changes.last().position
        val normalizedPosition = position.normalizeFromCanvas(canvasSize.value)
        val density = density.value.density

        val currentPoints = controlPoints.value
        val selectedIndex = currentPoints.indexOfFirst {
            Rect(it.toOffset(), curvePointSize * density * 2.0f).contains(position)
        }
        if (selectedIndex != -1) {
            selectedPoint.value = SelectedPoint(index = selectedIndex, isMoving = true, isRemoved = false)
            val curvePoint = selectedCurve.value.getPoint(selectedIndex)
            pointType.value = curvePoint?.type ?: CurvePointType.SMOOTH
        }

        val curve = selectedCurve.value
        val curvePoints = curve.points.value
        if (selectedIndex == -1) {
            presetsState.deselectCurrent()
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

    fun onCurveChannelChange(channel: ColorChannel) {
        selectedPoint.value = null
        currentChannel.value = channel
    }

    fun onPointsReset() {
        when (currentChannel.value) {
            ColorChannel.VALUE -> colorCurve.resetPoints()
            ColorChannel.RED -> redCurve.resetPoints()
            ColorChannel.GREEN -> greenCurve.resetPoints()
            ColorChannel.BLUE -> blueCurve.resetPoints()
        }
        presetsState.deselectCurrent()
    }

    fun onAllPointsReset() {
        colorCurve.resetPoints()
        redCurve.resetPoints()
        greenCurve.resetPoints()
        blueCurve.resetPoints()
        presetsState.deselectCurrent()
    }

    fun onPointTypeChange(type: CurvePointType) {
        pointType.value = type
        selectedPoint.value?.let { selectedCurve.value.updatePointType(it.index, type) }
    }

    fun onSelectedPointOffsetChange(point: SelectedPoint, offset255: IntOffset) {
        val curve = selectedCurve.value
        val curvePoint = curve.getPoint(point.index) ?: return

        val normalized = Offset(
            (offset255.x.toFloat() / 255).coerceIn(0f, 1f),
            (offset255.y.toFloat() / 255).coerceIn(0f, 1f)
        )
        curve.updatePoint(point.index, curvePoint.copy(x = normalized.x, y = normalized.y))
    }
}

data class CurveDrawData(
    val referenceLine: Path,
    val colorCurve: Path,
    val redCurve: Path,
    val greenCurve: Path,
    val blueCurve: Path,
    val points: List<CurvePoint>,
) {
    companion object {
        val EMPTY = CurveDrawData(
            referenceLine = Path(),
            colorCurve = Path(),
            redCurve = Path(),
            greenCurve = Path(),
            blueCurve = Path(),
            points = emptyList()
        )
    }
}

data class SelectedPoint(
    val index: Int,
    val isMoving: Boolean,
    val isRemoved: Boolean,
)


