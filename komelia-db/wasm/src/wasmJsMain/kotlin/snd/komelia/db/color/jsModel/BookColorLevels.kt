package snd.komelia.db.color.jsModel

import io.github.snd_r.komelia.color.BookColorLevels
import io.github.snd_r.komelia.color.ColorLevelChannels
import io.github.snd_r.komelia.color.ColorLevelsConfig
import snd.komelia.db.makeJsObject
import snd.komelia.db.set
import snd.komga.client.book.KomgaBookId

external interface JsBookColorLevels : JsAny {
    val bookId: String
    val channels: JsColorLevelChannels
}


internal fun JsBookColorLevels.toBookColorLevels() =
    BookColorLevels(
        bookId = KomgaBookId(this.bookId),
        channels = this.channels.toColorLevelChannels()
    )

internal fun BookColorLevels.toJs(): JsBookColorLevels {
    val jsObject = makeJsObject<JsBookColorLevels>()
    jsObject["bookId"] = this.bookId.value
    jsObject["channels"] = this.channels.toJs()
    return jsObject
}
