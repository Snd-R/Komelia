package io.github.snd_r.komelia.ui.settings.komf.general

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.settings.komf.KomfConfigState
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komf.api.PatchValue.Some
import snd.komf.api.config.EventListenerConfigUpdateRequest
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.config.KomgaConfigUpdateRequest
import snd.komf.client.KomfConfigClient
import snd.komga.client.library.KomgaLibrary
import snd.komga.client.library.KomgaLibraryId

class KomfSettingsViewModel(
    private val komfConfigClient: KomfConfigClient,
    private val appNotifications: AppNotifications,
    private val settingsRepository: SettingsRepository,
    val komfConfig: KomfConfigState,
    val libraries: StateFlow<List<KomgaLibrary>>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    var komfEnabled by mutableStateOf(false)
        private set
    var komfMode by mutableStateOf(KomfMode.REMOTE)
        private set
    var komfUrl by mutableStateOf("http://localhost:8085")
        private set
    var komfConnectionError by mutableStateOf<String?>(null)

    var komgaBaseUrl by mutableStateOf("localhost:25600")
        private set
    var komgaUsername by mutableStateOf("")
        private set

    var enableEventListener by mutableStateOf(false)
        private set
    var metadataLibraryFilters by mutableStateOf(emptyList<KomgaLibraryId>())
        private set
    var notificationsLibraryFilters by mutableStateOf(emptyList<KomgaLibraryId>())
        private set

    suspend fun initialize() {
        komfEnabled = settingsRepository.getKomfEnabled().first()
        komfMode = settingsRepository.getKomfMode().first()
        komfUrl = settingsRepository.getKomfUrl().first()

        appNotifications.runCatchingToNotifications { komfConfig.getConfig() }
            .onFailure {
                mutableState.value = LoadState.Error(it)
                komfConnectionError = "${it::class.simpleName}: ${it.message}"
            }.onSuccess { config ->
                komfConnectionError = null
                mutableState.value = LoadState.Success(Unit)
                config.onEach { initFields(it) }.launchIn(screenModelScope)
            }

        komfConfig.errorFlow
            .onEach {
                komfConnectionError = if (it == null) null
                else "${it::class.simpleName}: ${it.message}"
            }.launchIn(screenModelScope)
    }

    private fun initFields(config: KomfConfig) {
        komgaBaseUrl = config.komga.baseUri
        komgaUsername = config.komga.komgaUser
        enableEventListener = config.komga.eventListener.enabled
        metadataLibraryFilters = config.komga.eventListener.metadataLibraryFilter.map { KomgaLibraryId(it) }
        notificationsLibraryFilters = config.komga.eventListener.notificationsLibraryFilter.map { KomgaLibraryId(it) }
    }

    fun onKomfEnabledChange(enabled: Boolean) {
        this.komfEnabled = enabled
        screenModelScope.launch { settingsRepository.putKomfEnabled(enabled) }
    }

    fun onKomfModeChange(mode: KomfMode) {
        this.komfMode = mode
        screenModelScope.launch { settingsRepository.putKomfMode(mode) }
    }

    fun onKomfUrlChange(url: String) {
        this.komfUrl = url

        screenModelScope.launch {
            settingsRepository.putKomfUrl(url)
            appNotifications.runCatchingToNotifications { komfConfigClient.getConfig() }
                .onSuccess { komfConnectionError = null }
                .onFailure { komfConnectionError = "${it::class.simpleName}: ${it.message}" }
        }

    }

    fun onKomgaBaseUrlChange(url: String) {
        this.komgaBaseUrl = url
    }

    fun onKomgaUsernameChange(username: String) {
        this.komgaUsername = username
    }

    fun onKomgaPasswordUpdate(password: String) {

    }

    fun onEventListenerEnable(enable: Boolean) {
        enableEventListener = enable
        val eventListenerUpdate = EventListenerConfigUpdateRequest(enabled = Some(enable))
        val komgaUpdate = KomgaConfigUpdateRequest(eventListener = Some(eventListenerUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(komga = Some(komgaUpdate)))
    }

    fun onMetadataLibraryFilterSelect(libraryId: KomgaLibraryId) {
        metadataLibraryFilters = metadataLibraryFilters.addOrRemove(libraryId)

        val eventListenerUpdate = EventListenerConfigUpdateRequest(
            metadataLibraryFilter = Some(metadataLibraryFilters.map { it.value }),
        )
        val komgaUpdate = KomgaConfigUpdateRequest(eventListener = Some(eventListenerUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(komga = Some(komgaUpdate)))
    }

    fun onNotificationsLibraryFilterSelect(libraryId: KomgaLibraryId) {
        notificationsLibraryFilters = notificationsLibraryFilters.addOrRemove(libraryId)
        val eventListenerUpdate = EventListenerConfigUpdateRequest(
            notificationsLibraryFilter = Some(notificationsLibraryFilters.map { it.value })
        )
        val komgaUpdate = KomgaConfigUpdateRequest(eventListener = Some(eventListenerUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(komga = Some(komgaUpdate)))
    }

    private fun onConfigUpdate(request: KomfConfigUpdateRequest) {
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications { komfConfigClient.updateConfig(request) }
                .onFailure { mutableState.value = LoadState.Error(it) }

        }
    }

    private fun <T> List<T>.addOrRemove(value: T): List<T> {
        val mutable = this.toMutableList()
        val existingIndex = mutable.indexOf(value)
        if (existingIndex != -1) mutable.removeAt(existingIndex)
        else mutable.add(value)

        return mutable
    }
}