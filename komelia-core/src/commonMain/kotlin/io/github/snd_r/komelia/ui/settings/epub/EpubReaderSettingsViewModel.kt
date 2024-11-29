package io.github.snd_r.komelia.ui.settings.epub

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType.TTSU_EPUB
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EpubReaderSettingsViewModel(
    private val settingsRepository: EpubReaderSettingsRepository
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    val selectedEpubReader = MutableStateFlow(TTSU_EPUB)

    suspend fun initialize() {
        if (state.value !is LoadState.Uninitialized) return
        selectedEpubReader.value = settingsRepository.getReaderType().first()
        mutableState.value = LoadState.Success(Unit)
    }

    fun onSelectedTypeChange(type: EpubReaderType) {
        selectedEpubReader.value = type
        screenModelScope.launch { settingsRepository.putReaderType(type) }
    }
}

enum class EpubReaderType {
    TTSU_EPUB,
    KOMGA_EPUB,
}