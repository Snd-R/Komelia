package snd.komelia.color

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathSegment
import kotlin.math.abs

fun Path.denormalizeToCanvas(canvasSize: Size): Path {
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

fun Float.toCanvasX(canvasSize: Size) = this.toCanvasX(1f / canvasSize.width)
fun Float.toCanvasY(canvasSize: Size) = this.toCanvasY(1f / canvasSize.height)

private fun Float.toCanvasX(normalX: Float) = this / normalX
private fun Float.toCanvasY(normalY: Float): Float {
    return if (this > 1f) (1f - this) / normalY // position above the canvas
    else abs(this - 1f) / normalY
}

private fun Float.fromCanvasX(normalX: Float, width: Float) = this.coerceIn(0f, width) * normalX
private fun Float.fromCanvasY(normalY: Float, height: Float) = abs(this.coerceIn(0f, height) - height) * normalY

fun Offset.normalizeFromCanvas(canvasSize: Size): Offset = this.normalizeFromCanvas(1f, canvasSize)
fun Offset.normalizeFromCanvas(targetSize: Float, canvasSize: Size): Offset {
    val normalX = targetSize / canvasSize.width
    val normalY = targetSize / canvasSize.height
    return Offset(
        this.x.fromCanvasX(normalX, canvasSize.width),
        this.y.fromCanvasY(normalY, canvasSize.height)
    )
}

fun Offset.denormalizeToCanvas(canvasSize: Size): Offset {
    val normalX = 1f / canvasSize.width
    val normalY = 1f / canvasSize.height
    return Offset(
        this.x.toCanvasX(normalX),
        this.y.toCanvasY(normalY)
    )
}

fun CurvePoint.denormalizeToCanvas(targetSize: Size): CurvePoint {
    val normalX = 1f / targetSize.width
    val normalY = 1f / targetSize.height
    return CurvePoint(
        this.x.toCanvasX(normalX),
        this.y.toCanvasY(normalY),
        this.type
    )
}
