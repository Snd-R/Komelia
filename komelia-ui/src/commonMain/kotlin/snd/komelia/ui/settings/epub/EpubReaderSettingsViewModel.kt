package snd.komelia.ui.settings.epub

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import snd.komelia.settings.EpubReaderSettingsRepository
import snd.komelia.settings.model.EpubReaderType
import snd.komelia.settings.model.EpubReaderType.TTSU_EPUB
import snd.komelia.ui.LoadState

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
