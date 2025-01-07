package snd.komelia.db.color.jsModel

import io.github.snd_r.komelia.color.ColorCurvePreset
import snd.komelia.db.makeJsObject
import snd.komelia.db.set

external interface JsColorCurvePreset : JsAny {
    val name: String
    val points: JsColorCurvePoints
}

fun ColorCurvePreset.toJs(): JsColorCurvePreset {
    val jsObject = makeJsObject<JsColorCurvePreset>()
    jsObject["name"] = this.name
    jsObject["points"] = this.points.toJs()
    return jsObject
}

fun JsColorCurvePreset.toColorCurvePreset() =
    ColorCurvePreset(
        name = this.name,
        points = this.points.toColorCurvePoints()
    )

