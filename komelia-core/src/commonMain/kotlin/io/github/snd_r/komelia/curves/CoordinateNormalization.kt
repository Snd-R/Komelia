package io.github.snd_r.komelia.curves

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathSegment
import io.github.snd_r.komelia.curves.Curve.CurvePoint
import kotlin.math.abs

fun Path.denormalize(canvasSize: Size): Path {
    val newPath = Path()
    val normalX = 1f / canvasSize.width
    val normalY = 1f / canvasSize.height
    for (segment in this) {
        when (segment.type) {
            PathSegment.Type.Move -> {
                val x = segment.points[0].toCanvasX(normalX)
                val y = segment.points[1].toCanvasY(normalY)
                newPath.moveTo(x, y)
            }

            PathSegment.Type.Line -> {
                val x2 = segment.points[2].toCanvasX(normalX)
                val y2 = segment.points[3].toCanvasY(normalY)
                newPath.lineTo(x2, y2)
            }

            PathSegment.Type.Cubic -> {
                val x2 = segment.points[2].toCanvasX(normalX)
                val y2 = segment.points[3].toCanvasY(normalY)

                val x3 = segment.points[4].toCanvasX(normalX)
                val y3 = segment.points[5].toCanvasY(normalY)

                val x4 = segment.points[6].toCanvasX(normalX)
                val y4 = segment.points[7].toCanvasY(normalY)

                newPath.cubicTo(x2, y2, x3, y3, x4, y4)
            }

            PathSegment.Type.Quadratic -> {
                val x2 = segment.points[2].toCanvasX(normalX)
                val y2 = segment.points[3].toCanvasY(normalY)
                val x3 = segment.points[4].toCanvasX(normalX)
                val y3 = segment.points[5].toCanvasY(normalY)
                newPath.quadraticTo(x2, y2, x3, y3)
            }

            PathSegment.Type.Close -> newPath.close()
            PathSegment.Type.Done -> {}
            PathSegment.Type.Conic -> TODO()
        }
    }
    return newPath
}

private fun Float.toCanvasX(normalX: Float) = this / normalX
private fun Float.toCanvasY(normalY: Float): Float {
    return if (this > 1f) (1f - this) / normalY // position above the canvas
    else abs(this - 1f) / normalY
}

private fun Float.toNormalizedX(normalX: Float, width: Float) = this.coerceIn(0f, width) * normalX
private fun Float.toNormalizedY(normalY: Float, height: Float) = abs(this.coerceIn(0f, height) - height) * normalY

fun Offset.normalize(canvasSize: Size): Offset {
    val normalX = 1f / canvasSize.width
    val normalY = 1f / canvasSize.height
    return Offset(
        this.x.toNormalizedX(normalX, canvasSize.width),
        this.y.toNormalizedY(normalY, canvasSize.height)
    )
}

fun Offset.denormalize(canvasSize: Size): Offset {
    val normalX = 1f / canvasSize.width
    val normalY = 1f / canvasSize.height
    return Offset(
        this.x.toCanvasX(normalX),
        this.y.toCanvasY(normalY)
    )
}

fun CurvePoint.denormalize(canvasSize: Size): CurvePoint {
    val normalX = 1f / canvasSize.width
    val normalY = 1f / canvasSize.height
    return CurvePoint(
        this.x.toCanvasX(normalX),
        this.y.toCanvasY(normalY),
        this.type
    )
}
