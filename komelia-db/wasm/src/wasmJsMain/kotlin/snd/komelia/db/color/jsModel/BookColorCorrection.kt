package snd.komelia.db.color.jsModel

import io.github.snd_r.komelia.ui.color.ColorCorrectionType
import snd.komelia.db.makeJsObject
import snd.komelia.db.set
import snd.komga.client.book.KomgaBookId

external interface JsBookColorCorrection : JsAny {
    val bookId: String
    val type: String
}

fun jsBookColorCorrection(bookId: KomgaBookId, type: ColorCorrectionType): JsBookColorCorrection {
    val jsObject = makeJsObject<JsBookColorCorrection>()
    jsObject["bookId"] = bookId.value
    jsObject["type"] = type.name
    return jsObject
}