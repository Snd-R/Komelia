package snd.komelia.color.repository

import snd.komelia.color.ColorLevelsPreset

interface ColorLevelsPresetRepository {
    suspend fun getPresets(): List<ColorLevelsPreset>
    suspend fun savePreset(preset: ColorLevelsPreset)
    suspend fun deletePreset(preset: ColorLevelsPreset)
}
