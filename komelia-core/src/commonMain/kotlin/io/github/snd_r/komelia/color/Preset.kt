package io.github.snd_r.komelia.color

import kotlinx.serialization.Serializable


sealed interface Preset {
    val name: String
}

@Serializable
data class ColorCurvePreset(
    override val name: String,
    val points: ColorCurvePoints
) : Preset

@Serializable
data class ColorLevelsPreset(
    override val name: String,
    val channels: ColorLevelChannels
) : Preset
