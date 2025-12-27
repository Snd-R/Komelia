package snd.komelia.color

import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.Serializable
import snd.komelia.color.CurvePointType.SMOOTH
import snd.komga.client.book.KomgaBookId

val defaultPoints = listOf(
    CurvePoint(0f, 0f, SMOOTH),
    CurvePoint(1f, 1f, SMOOTH),
)

@Serializable
data class ColorCurveBookPoints(
    val bookId: KomgaBookId,
    val channels: ColorCurvePoints
)

@Serializable
data class ColorCurvePoints(
    val colorCurvePoints: List<CurvePoint>,
    val redCurvePoints: List<CurvePoint>,
    val greenCurvePoints: List<CurvePoint>,
    val blueCurvePoints: List<CurvePoint>,
) {
    companion object {
        val DEFAULT = ColorCurvePoints(defaultPoints, defaultPoints, defaultPoints, defaultPoints)
    }
}

@Serializable
data class CurvePoint(
    val x: Float,
    val y: Float,
    val type: CurvePointType
) {
    constructor(offset: Offset, type: CurvePointType) : this(offset.x, offset.y, type)

    fun toOffset(): Offset = Offset(x, y)
}

enum class CurvePointType {
    SMOOTH,
    CORNER,
}

