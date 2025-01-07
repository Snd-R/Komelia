package snd.komelia.db.color.jsModel

import io.github.snd_r.komelia.color.ColorLevelsPreset
import snd.komelia.db.makeJsObject
import snd.komelia.db.set

external interface JsColorLevelsPreset : JsAny {
    val name: String
    val channels: JsColorLevelChannels
}

fun ColorLevelsPreset.toJs(): JsColorLevelsPreset {
    val jsObject = makeJsObject<JsColorLevelsPreset>()
    jsObject["name"] = this.name
    jsObject["channels"] = this.channels.toJs()
    return jsObject
}

fun JsColorLevelsPreset.toColorLevelsPreset() =
    ColorLevelsPreset(
        name = this.name,
        channels = this.channels.toColorLevelChannels()
    )

