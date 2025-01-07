package io.github.snd_r.komelia.color.repository

import io.github.snd_r.komelia.color.ColorCurvePreset

interface ColorCurvePresetRepository {
    suspend fun getPresets(): List< ColorCurvePreset>
    suspend fun savePreset(preset: ColorCurvePreset)
    suspend fun deletePreset(preset: ColorCurvePreset)
}
