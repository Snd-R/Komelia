package snd.komelia.ui.settings.komf.mangabaka

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komelia.AppNotifications
import snd.komelia.settings.KomfSettingsRepository
import snd.komelia.ui.LoadState
import snd.komelia.ui.error.formatExceptionMessage
import snd.komelia.ui.settings.komf.KomfSharedState
import snd.komf.api.config.DownloadProgress
import snd.komf.api.config.MangaBakaDatabaseDto
import snd.komf.client.KomfConfigClient

class KomfMangaBakaSettingsViewModel(
    private val settingsRepository: KomfSettingsRepository,
    private val configClient: KomfConfigClient,
    private val appNotifications: AppNotifications,
    val komfSharedState: KomfSharedState,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    val errorMessage = state.combine(komfSharedState.configError) { state, sharedError ->
        when {
            state is LoadState.Error -> formatExceptionMessage(state.exception)
            sharedError != null -> formatExceptionMessage(sharedError)
            else -> null
        }
    }.stateIn(screenModelScope, SharingStarted.Eagerly, null)
    val enabled = MutableStateFlow(false)

    var mangaBakaDbInfo by mutableStateOf<MangaBakaDatabaseDto?>(null)
        private set

    suspend fun initialize() {
        if (state.value !is LoadState.Uninitialized) return
        enabled.value = settingsRepository.getMangaBakaEnabled().first()
        mutableState.value = LoadState.Success(Unit)

        appNotifications.runCatchingToNotifications { komfSharedState.getConfig() }
            .onFailure { mutableState.value = LoadState.Error(it) }
            .onSuccess { config ->
                config
                    .onEach { mangaBakaDbInfo = it.metadataProviders.mangaBakaDatabase }
                    .launchIn(screenModelScope)
            }
    }

    fun onEnabledChange(enabled: Boolean) {
        screenModelScope.launch {
            settingsRepository.putMangaBakaEnabled(enabled)
            this@KomfMangaBakaSettingsViewModel.enabled.value = enabled
        }
    }

    fun onMangaBakaDbUpdate(): Flow<DownloadProgress> {
        return configClient.updateMangaBakaDb()
            .flowOn(Dispatchers.Default)
            .onCompletion { komfSharedState.loadConfig() }
    }
}