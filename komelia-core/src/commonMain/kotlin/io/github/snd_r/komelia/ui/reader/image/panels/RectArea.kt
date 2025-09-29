package io.github.snd_r.komelia.ui.reader.image.panels

import androidx.compose.ui.geometry.Rect
import snd.komelia.image.ImageRect
import kotlin.math.max
import kotlin.math.min

fun areaOfRects(rects: List<Rect>): Float {
    val nonZeroRects = rects.filter { it.area() != 0f }
    val xDividers = nonZeroRects.map { listOf(it.left, it.right) }.flatten().distinct().sorted()
    val splitRects = rectsSplitAtXDividers(nonZeroRects, xDividers)
    val combinedRects = combinedRectsOnY(splitRects, xDividers)
    val area = combinedRects.fold(0f) { acc, rect -> acc + rect.area() }
    return area
}

fun ImageRect.toRect() = Rect(
    left = this.left.toFloat(),
    top = this.top.toFloat(),
    right = this.right.toFloat(),
    bottom = this.bottom.toFloat()
)

private fun Rect.area() = this.width * this.height

private fun Rect.splitAtX(x: Float): List<Rect> {
    if (x <= this.left || x >= this.right) {
        return listOf(this)
    }
    val res = listOf(
        this.copy(right = x),
        this.copy(left = x)
    )
    return res
}

fun rectsSplitAtXDividers(rects: List<Rect>, xDividers: List<Float>): List<Rect> {
    var dividedRects: List<Rect> = rects
    for (xDivider in xDividers) {
        val running = mutableListOf<Rect>()
        for (rect in dividedRects) {
            val dividedInputRects = rect.splitAtX(xDivider)
            running.addAll(dividedInputRects)
        }
        dividedRects = running
    }

    return dividedRects
}

fun combinedRectsOnY(rects: List<Rect>, xDividers: List<Float>): List<Rect> {
    val combinedRects = mutableListOf<Rect>()
    for (xDivider in xDividers) {
        val xFilteredRects = rects.filter { it.left == xDivider }
        val sortedRects = xFilteredRects.sortedWith(compareBy { it.top })
        val first = sortedRects.firstOrNull()
        val last = sortedRects.lastOrNull()
        if (first == null || last == null) continue
        if (first == last) {
            combinedRects.add(first)
            continue
        }
        var prev: Rect = first
        for (rect in sortedRects) {
            if (rect.overlaps(prev)) {
                prev = rect.union(prev)
            } else {
                combinedRects.add(prev)
                prev = rect
            }
        }
        combinedRects.add(prev)
    }
    return combinedRects
}

private fun Rect.union(other: Rect): Rect {
    val union = Rect(
        left = min(this.left, other.left),
        top = min(this.top, other.top),
        right = max(this.right, other.right),
        bottom = max(this.bottom, other.bottom)
    )
    return union
}