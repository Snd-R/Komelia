package io.github.snd_r.komelia.ui.color

import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.color.ColorCurvePoints
import io.github.snd_r.komelia.color.ColorCurvePreset
import io.github.snd_r.komelia.color.ColorLevelChannels
import io.github.snd_r.komelia.color.ColorLevelsPreset
import io.github.snd_r.komelia.color.Preset
import io.github.snd_r.komelia.color.repository.ColorCurvePresetRepository
import io.github.snd_r.komelia.color.repository.ColorLevelsPresetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class PresetsState<T : Preset>(
    protected val appNotifications: AppNotifications,
    protected val coroutineScope: CoroutineScope,
) {
    val selectedPreset = MutableStateFlow<T?>(null)
    val presets = MutableStateFlow<List<T>>(emptyList())

    suspend fun initialize() {
        presets.value = getPresets()
    }

    fun onPresetSelect(preset: T) {
        val selectedPreset = presets.value.firstOrNull { it.name == preset.name }
        if (selectedPreset == null) {
            appNotifications.add(AppNotification.Error("preset with $selectedPreset does not exist"))
            return
        }
        this.selectedPreset.value = selectedPreset
        presetSelect(selectedPreset)
    }

    fun onPresetDelete(preset: T) {
        val deletePreset = presets.value.firstOrNull { it.name == preset.name }
        if (deletePreset == null) {
            appNotifications.add(AppNotification.Error("preset with $preset does not exist"))
            return
        }
        presets.update { it.minus(preset) }
        selectedPreset.value = null
        coroutineScope.launch { presetDelete(preset) }
    }


    fun onPresetAdd(presetName: String, override: Boolean = false) {
        val existingPreset = presets.value.firstOrNull { it.name == presetName }
        if (existingPreset != null && !override) {
            appNotifications.add(AppNotification.Error("Preset with that name already exists"))
            return
        }

        coroutineScope.launch {
            val preset = presetSave(presetName)
            if (existingPreset != null) {
                presets.update { presets ->
                    val updated = presets.toMutableList()
                    val index = updated.indexOfFirst { it.name == preset.name }
                    updated[index] = preset
                    updated
                }
            } else {
                presets.update { it.plus(preset) }
            }
            onPresetSelect(preset)
        }
    }

    fun deselectCurrent() {
        selectedPreset.value = null
    }

    protected abstract suspend fun getPresets(): List<T>
    protected abstract suspend fun presetSave(name: String): T
    protected abstract suspend fun presetUpdate(name: String): T

    protected abstract suspend fun presetDelete(preset: T)
    protected abstract fun presetSelect(preset: T)
}

class CurvePresetsState(
    private val presetRepository: ColorCurvePresetRepository,
    private val points: StateFlow<ColorCurvePoints>,
    private val onPointsChange: (ColorCurvePoints) -> Unit,
    appNotifications: AppNotifications,
    coroutineScope: CoroutineScope,
) : PresetsState<ColorCurvePreset>(
    appNotifications,
    coroutineScope
) {
    override suspend fun getPresets(): List<ColorCurvePreset> {
        return presetRepository.getPresets()
    }

    override suspend fun presetSave(name: String): ColorCurvePreset {
        val preset = ColorCurvePreset(name, points.value)
        presetRepository.savePreset(preset)
        return preset
    }

    override suspend fun presetUpdate(name: String): ColorCurvePreset {
        val preset = ColorCurvePreset(name, points.value)
        presetRepository.savePreset(preset)
        return preset
    }

    override suspend fun presetDelete(preset: ColorCurvePreset) {
        presets.value.firstOrNull { it.name == preset.name }?.let {
            presetRepository.deletePreset(it)
        }
    }

    override fun presetSelect(preset: ColorCurvePreset) {
        presets.value.firstOrNull { it.name == preset.name }?.let {
            onPointsChange(it.points)
        }
    }

}

class LevelsPresetsState(
    private val presetRepository: ColorLevelsPresetRepository,
    private val config: StateFlow<ColorLevelChannels>,
    private val onChange: (ColorLevelChannels) -> Unit,
    appNotifications: AppNotifications,
    coroutineScope: CoroutineScope,
) : PresetsState<ColorLevelsPreset>(
    appNotifications,
    coroutineScope
) {
    override suspend fun getPresets(): List<ColorLevelsPreset> {
        return presetRepository.getPresets()
    }

    override suspend fun presetSave(name: String): ColorLevelsPreset {
        val preset = ColorLevelsPreset(name, config.value)
        presetRepository.savePreset(preset)
        return preset
    }

    override suspend fun presetUpdate(name: String): ColorLevelsPreset {
        val preset = ColorLevelsPreset(name, config.value)
        presetRepository.savePreset(preset)
        return preset
    }

    override suspend fun presetDelete(preset: ColorLevelsPreset) {
        presets.value.firstOrNull { it.name == preset.name }?.let {
            presetRepository.deletePreset(it)
        }
    }

    override fun presetSelect(preset: ColorLevelsPreset) {
        presets.value.firstOrNull { it.name == preset.name }?.let {
            onChange(it.channels)
        }
    }

}
