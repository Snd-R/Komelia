package io.github.snd_r.komelia.color

import androidx.compose.ui.graphics.Path
import io.github.snd_r.komelia.color.CurvePointType.CORNER
import io.github.snd_r.komelia.color.CurvePointType.SMOOTH
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

class Curve(initialPoints: List<CurvePoint> = defaultPoints) {
    private val _points = MutableStateFlow(initialPoints)
    private val curveState = _points.map { createCurve(it) }
    val points = _points.asStateFlow()
    val path = curveState.map { it.path }
    val lookupTable = curveState.map { calculateLookupTable(it.samples) }

    fun getPoint(index: Int): CurvePoint? {
        return _points.value.getOrNull(index)
    }

    fun addPoint(point: CurvePoint) {
        _points.update { it.toMutableList().apply { add(point) } }
    }

    fun addPoint(index: Int, point: CurvePoint) {
        _points.update { it.toMutableList().apply { add(index, point) } }
    }

    fun updatePoint(index: Int, point: CurvePoint) {
        _points.update { it.toMutableList().apply { set(index, point) } }
    }

    fun updatePointType(index: Int, type: CurvePointType) {
        _points.update {
            it.toMutableList().apply {
                getOrNull(index)?.let { set(index, it.copy(type = type)) }
            }
        }
    }

    fun removePoint(index: Int) {
        _points.update { it.toMutableList().apply { removeAt(index) } }
    }

    fun resetPoints() {
        _points.value = listOf(CurvePoint(0f, 0f, SMOOTH), CurvePoint(1f, 1f, SMOOTH))
    }

    fun setPoints(points: List<CurvePoint>) {
        _points.value = points
    }

    private fun calculateLookupTable(samples: FloatArray): UByteArray? {
        val points = _points.value
        if (points.size == 2 &&
            points[0].x == 0.0f && points[0].y == 0.0f &&
            points[1].x == 1.0f && points[1].y == 1.0f
        ) {
            return null
        }
        val lut = UByteArray(samples.size)
        for ((index, sample) in samples.withIndex()) {
            lut[index] = (sample * (samples.size - 1)).roundToInt().toUByte()
        }
        return lut
    }

    private fun createCurve(points: List<CurvePoint>): CurveState {
        val state = CurveState()
        val sampleCount = state.sampleCount
        val samples = state.samples
        val path = state.path
        if (points.isEmpty()) return state

        points.first().let {
            val boundary = (it.x * (sampleCount - 1)).roundToInt()
            for (i in 0 until boundary) {
                samples[i] = it.y
            }
        }
        points.last().let {
            val boundary = (it.x * (sampleCount - 1)).roundToInt()
            for (i in boundary until sampleCount) {
                samples[i] = it.y
            }
        }

        var start: CurvePoint = points.first()
        if (start.x != 0f) {
            path.moveTo(0f, start.y)
            path.lineTo(start.x, start.y)
        } else {
            path.moveTo(start.x, start.y)
        }
        val remainingPoints = points.drop(1)

        var previous: CurvePoint? = null
        var next: CurvePoint?
        for ((index, currentPoint) in remainingPoints.withIndex()) {

            if (start.type == CORNER) {
                previous = start
            }

            if (currentPoint.type == CORNER) {
                next = currentPoint
            } else {
                next = remainingPoints.getOrNull(index + 1)
            }

            state.addCurve(
                CurvePointsWindow(
                    start = start,
                    end = currentPoint,
                    previous = previous,
                    next = next
                )
            )
            previous = start
            start = currentPoint
        }
        for (point in points) {
            val index = (point.x * (sampleCount - 1)).roundToInt()
            samples[index] = point.y
        }
        points.lastOrNull()?.let { last ->
            if (last.x < 1.0f) {
                path.lineTo(1f, last.y)
            }
        }
        return state
    }


    private class CurveState(
        val sampleCount: Int = 256,
        val samples: FloatArray = FloatArray(sampleCount),
        val path: Path = Path()
    ) {
        fun addCurve(points: CurvePointsWindow) {
            val dx = points.end.x - points.start.x
            val dy = points.end.y - points.start.y
            val x0 = points.start.x
            val y0 = points.start.y
            val x3 = points.end.x
            val y3 = points.end.y

            val x1 = 2.0f / 3.0f * points.start.x + 1.0f / 3.0f * points.end.x
            val x2 = 1.0f / 3.0f * points.start.x + 2.0f / 3.0f * points.end.x

            var y1 = 0.0f
            var y2 = 0.0f
            when {
                points.previous == null && points.next == null -> {
                    y1 = y0 + dy / 3.0f
                    y2 = y0 + dy * 2.0f / 3.0f
                    path.lineTo(points.end.x, points.end.y)
                }

                points.previous == null && points.next != null -> {
                    val slope = (points.next.y - y0) / (points.next.x - x0)
                    y2 = y3 - slope * dx / 3.0f
                    y1 = y0 + (y2 - y0) / 2.0f
                    path.cubicTo(
                        x1,
                        y1,
                        x2,
                        y2,
                        points.end.x,
                        points.end.y
                    )
                }

                points.previous != null && points.next == null -> {
                    val slope = (y3 - points.previous.y) / (x3 - points.previous.x)
                    y1 = y0 + slope * dx / 3.0f
                    y2 = y3 + (y1 - y3) / 2.0f
                    path.cubicTo(
                        x1,
                        y1,
                        x2,
                        y2,
                        points.end.x,
                        points.end.y
                    )

                }

                points.previous != null && points.next != null -> {
                    val slope1 = (points.end.y - points.previous.y) / (points.end.x - points.previous.x)
                    y1 = points.start.y + slope1 * dx / 3.0f
                    val slope2 = (points.next.y - points.start.y) / (points.next.x - points.start.x)
                    y2 = points.end.y - slope2 * dx / 3.0f
                    path.cubicTo(x1, y1, x2, y2, points.end.x, points.end.y)
                }
            }

            for (i in 0..(dx * (sampleCount - 1)).roundToInt()) {
                val t = i / dx / (sampleCount - 1).toFloat()
                val y = y0 * (1 - t) * (1 - t) * (1 - t) +
                        3 * y1 * (1 - t) * (1 - t) * t +
                        3 * y2 * (1 - t) * t * t +
                        y3 * t * t * t

                val index = i + (x0 * (sampleCount - 1)).roundToInt()
                if (index < sampleCount) {
                    samples[index] = y.coerceIn(0.0f, 1.0f)
                }
            }
        }
    }

    private data class CurvePointsWindow(
        val start: CurvePoint,
        val end: CurvePoint,
        val previous: CurvePoint? = null,
        val next: CurvePoint? = null,
    )
}
