package snd.komelia.db.color.jsModel

import io.github.snd_r.komelia.color.ColorCurvePoints
import io.github.snd_r.komelia.color.CurvePoint
import io.github.snd_r.komelia.color.CurvePointType
import snd.komelia.db.makeJsObject
import snd.komelia.db.set

external interface JsColorCurvePoints : JsAny {
    val colorCurvePoints: JsArray<JsCurvePoint>
    val redCurvePoints: JsArray<JsCurvePoint>
    val greenCurvePoints: JsArray<JsCurvePoint>
    val blueCurvePoints: JsArray<JsCurvePoint>
}

external interface JsCurvePoint : JsAny {
    val x: Double
    val y: Double
    val type: String
}

internal fun ColorCurvePoints.toJs(): JsColorCurvePoints {
    val jsObject = makeJsObject<JsColorCurvePoints>()
    jsObject["colorCurvePoints"] = this.colorCurvePoints.toJsArray()
    jsObject["redCurvePoints"] = this.redCurvePoints.toJsArray()
    jsObject["greenCurvePoints"] = this.greenCurvePoints.toJsArray()
    jsObject["blueCurvePoints"] = this.blueCurvePoints.toJsArray()
    return jsObject
}

internal fun JsColorCurvePoints.toColorCurvePoints() = ColorCurvePoints(
    colorCurvePoints = colorCurvePoints.toCurvePoints(),
    redCurvePoints = redCurvePoints.toCurvePoints(),
    greenCurvePoints = greenCurvePoints.toCurvePoints(),
    blueCurvePoints = blueCurvePoints.toCurvePoints()
)

internal fun List<CurvePoint>.toJsArray(): JsArray<JsCurvePoint> =
    this.map { point ->
        val jsPoint = makeJsObject<JsCurvePoint>()
        jsPoint["x"] = point.x.toDouble().toJsNumber()
        jsPoint["y"] = point.y.toDouble().toJsNumber()
        jsPoint["type"] = point.type.name
        jsPoint
    }.toJsArray()

internal fun JsArray<JsCurvePoint>.toCurvePoints(): List<CurvePoint> =
    this.toList().map {
        CurvePoint(
            x = it.x.toFloat(),
            y = it.y.toFloat(),
            type = CurvePointType.valueOf(it.type)
        )
    }
