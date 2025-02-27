package io.github.snd_r.komelia.ui.settings.komf.general

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.error.formatExceptionMessage
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.ui.settings.komf.KomfSharedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.client.KomfConfigClient
import snd.komf.client.KomfMediaServerClient

class KomfSettingsViewModel(
    private val komfConfigClient: KomfConfigClient,
    komgaMediaServerClient: KomfMediaServerClient,
    kavitaMediaServerClient: KomfMediaServerClient?,
    private val appNotifications: AppNotifications,
    private val settingsRepository: CommonSettingsRepository,
    private val integrationToggleEnabled: Boolean,
    val komfSharedState: KomfSharedState,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    private val configListenerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val komfEnabled = MutableStateFlow(false)
    val komfMode = MutableStateFlow(KomfMode.REMOTE)
    val komfUrl = MutableStateFlow("http://localhost:8085")

    val komfConnectionError = komfSharedState.configError
        .map { error -> error?.let { formatExceptionMessage(it) } }
        .stateIn(screenModelScope, SharingStarted.Eagerly, null)

    val komgaConnectionState = KomgaConnectionState(
        coroutineScope = configListenerScope,
        komfMediaServerClient = komgaMediaServerClient,
        komfSharedState = komfSharedState,
        onConfigUpdate = this::updateConfig
    )
    val kavitaConnectionState = kavitaMediaServerClient?.let {
        KavitaConnectionState(
            coroutineScope = configListenerScope,
            komfMediaServerClient = kavitaMediaServerClient,
            komfSharedState = komfSharedState,
            onConfigUpdate = this::updateConfig
        )
    }

    suspend fun initialize() {
        komfEnabled.value = settingsRepository.getKomfEnabled().first()
        komfMode.value = settingsRepository.getKomfMode().first()
        komfUrl.value = settingsRepository.getKomfUrl().first()

        if (!integrationToggleEnabled || komfEnabled.value) {
            launchConfigFlowListener()
        }
        mutableState.value = LoadState.Success(Unit)
    }

    private suspend fun launchConfigFlowListener() {
        configListenerScope.coroutineContext.cancelChildren()
        komfSharedState.getConfig()
            .onEach {
                komgaConnectionState.initialize(it)
                komgaConnectionState.checkConnection()
                kavitaConnectionState?.initialize(it)
                kavitaConnectionState?.checkConnection()
            }
            .launchIn(configListenerScope)
    }

    fun onKomfEnabledChange(enabled: Boolean) {
        this.komfEnabled.value = enabled

        screenModelScope.launch {
            settingsRepository.putKomfEnabled(enabled)
            if (enabled) {
                launchConfigFlowListener()
            } else {
                configListenerScope.coroutineContext.cancelChildren()
            }
        }
    }

    fun onKomfUrlChange(url: String) {
        this.komfUrl.value = url

        screenModelScope.launch {
            settingsRepository.putKomfUrl(url)
            komfSharedState.loadConfig()
        }

    }

    private suspend fun updateConfig(request: KomfConfigUpdateRequest) {
        appNotifications.runCatchingToNotifications {
            komfConfigClient.updateConfig(request)
        }.onFailure { mutableState.value = LoadState.Error(it) }
    }

    override fun onDispose() {
        configListenerScope.cancel()
    }
}
