package snd.komelia.db.color.jsModel

import io.github.snd_r.komelia.color.ColorCurveBookPoints
import io.github.snd_r.komelia.color.ColorCurvePoints
import io.github.snd_r.komelia.color.CurvePoint
import io.github.snd_r.komelia.color.CurvePointType
import snd.komelia.db.makeJsObject
import snd.komelia.db.set
import snd.komga.client.book.KomgaBookId

external interface JsColorCurveBookPoints : JsAny {
    val bookId: String
    val channels: JsColorCurvePoints
}


fun JsColorCurveBookPoints.toColorCurveBookPoints(): ColorCurveBookPoints =
    ColorCurveBookPoints(
        bookId = KomgaBookId(this.bookId),
        channels = ColorCurvePoints(
            colorCurvePoints = this.channels.colorCurvePoints.toCurvePoints(),
            redCurvePoints = this.channels.redCurvePoints.toCurvePoints(),
            greenCurvePoints = this.channels.redCurvePoints.toCurvePoints(),
            blueCurvePoints = this.channels.blueCurvePoints.toCurvePoints(),
        )
    )

internal fun ColorCurveBookPoints.toJs(): JsColorCurveBookPoints {
    val jsObject = makeJsObject<JsColorCurveBookPoints>()
    jsObject["bookId"] = this.bookId.value
    jsObject["channels"] = this.channels.toJs()
    return jsObject
}
