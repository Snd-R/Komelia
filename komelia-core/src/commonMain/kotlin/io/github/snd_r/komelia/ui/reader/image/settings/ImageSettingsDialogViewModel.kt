package io.github.snd_r.komelia.ui.reader.image.settings

import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ImageSettingsDialogViewModel(
//    private val curvesState: CurvesViewModel
    private val settingsRepository: CommonSettingsRepository,
    private val readerSettingsRepository: ImageReaderSettingsRepository,
    private val decoderDescriptor: Flow<PlatformDecoderDescriptor>,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val generalState = ImageSettingsGeneralState(
        settingsRepository = settingsRepository,
        readerSettingsRepository = readerSettingsRepository,
        decoderDescriptor = decoderDescriptor,
        coroutineScope = coroutineScope
    )
    private val generalTab = ImageSettingsGeneralTab(generalState)
    val currentTab = MutableStateFlow<DialogTab>(generalTab)
    val tabs: List<DialogTab> = listOf(generalTab)

    suspend fun initialize() {
        generalState.initialize()
    }

    fun onTabChange(tab: DialogTab) {
        currentTab.value = tab
    }

    fun onDispose() {
        coroutineScope.cancel()
    }
}