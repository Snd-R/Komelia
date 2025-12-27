package snd.komelia.db.color.jsModel

import io.github.snd_r.komelia.color.ColorLevelChannels
import io.github.snd_r.komelia.color.ColorLevelsConfig
import snd.komelia.db.makeJsObject
import snd.komelia.db.set

external interface JsColorLevelChannels : JsAny {
    val color: JsColorLevelsConfig
    val red: JsColorLevelsConfig
    val green: JsColorLevelsConfig
    val blue: JsColorLevelsConfig
}

external interface JsColorLevelsConfig : JsAny {
    val lowInput: Double
    val highInput: Double
    val lowOutput: Double
    val highOutput: Double
    val gamma: Double
}


internal fun JsColorLevelChannels.toColorLevelChannels() =
    ColorLevelChannels(
        color = color.toColorLevelConfig(),
        red = red.toColorLevelConfig(),
        green = green.toColorLevelConfig(),
        blue = blue.toColorLevelConfig(),
    )

internal fun JsColorLevelsConfig.toColorLevelConfig() =
    ColorLevelsConfig(
        lowInput = lowInput.toFloat(),
        highInput = highInput.toFloat(),
        lowOutput = lowOutput.toFloat(),
        highOutput = highOutput.toFloat(),
        gamma = gamma.toFloat()
    )


internal fun ColorLevelChannels.toJs(): JsColorLevelChannels {
    val jsObject = makeJsObject<JsColorLevelChannels>()
    jsObject["color"] = this.color.toJs()
    jsObject["red"] = this.red.toJs()
    jsObject["green"] = this.green.toJs()
    jsObject["blue"] = this.blue.toJs()
    return jsObject
}

internal fun ColorLevelsConfig.toJs(): JsColorLevelsConfig {
    val jsObject = makeJsObject<JsColorLevelsConfig>()
    jsObject["lowInput"] = this.lowInput.toDouble().toJsNumber()
    jsObject["highInput"] = this.highInput.toDouble().toJsNumber()
    jsObject["lowOutput"] = this.lowOutput.toDouble().toJsNumber()
    jsObject["highOutput"] = this.highOutput.toDouble().toJsNumber()
    jsObject["gamma"] = this.gamma.toDouble().toJsNumber()
    return jsObject
}
