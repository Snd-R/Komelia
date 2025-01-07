package io.github.snd_r.komelia.color.repository

import io.github.snd_r.komelia.color.ColorLevelsPreset

interface ColorLevelsPresetRepository {
    suspend fun getPresets(): List<ColorLevelsPreset>
    suspend fun savePreset(preset: ColorLevelsPreset)
    suspend fun deletePreset(preset: ColorLevelsPreset)
}
