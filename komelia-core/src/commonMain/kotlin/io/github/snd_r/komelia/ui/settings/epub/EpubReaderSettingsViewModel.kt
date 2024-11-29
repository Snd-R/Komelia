package io.github.snd_r.komelia.ui.settings.epub

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow

class EpubReaderSettingsViewModel : ScreenModel {
    val selectedEpubReader = MutableStateFlow(EpubReaderType.TTSU)
}

enum class EpubReaderType {
    KOMGA,
    TTSU
}