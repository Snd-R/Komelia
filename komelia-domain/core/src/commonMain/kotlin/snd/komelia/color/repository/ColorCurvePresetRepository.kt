package snd.komelia.color.repository

import snd.komelia.color.ColorCurvePreset

interface ColorCurvePresetRepository {
    suspend fun getPresets(): List< ColorCurvePreset>
    suspend fun savePreset(preset: ColorCurvePreset)
    suspend fun deletePreset(preset: ColorCurvePreset)
}
