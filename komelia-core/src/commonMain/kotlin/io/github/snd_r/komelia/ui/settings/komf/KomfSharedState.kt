package io.github.snd_r.komelia.ui.settings.komf

import io.github.snd_r.komelia.AppNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import snd.komf.api.config.KomfConfig
import snd.komf.api.mediaserver.KomfMediaServerLibrary
import snd.komf.client.KomfConfigClient
import snd.komf.client.KomfMediaServerClient

class KomfSharedState(
    private val komfConfigClient: KomfConfigClient,
    private val komgaServerClient: KomfMediaServerClient,
    private val kavitaServerClient: KomfMediaServerClient,
    private val notifications: AppNotifications
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val komgaLibraries = MutableStateFlow<List<KomfMediaServerLibrary>>(emptyList())
    private val kavitaLibraries = MutableStateFlow<List<KomfMediaServerLibrary>>(emptyList())
    private val config = MutableStateFlow<KomfConfig?>(null)
    val configError = MutableStateFlow<Throwable?>(null)

    suspend fun getConfig(): Flow<KomfConfig> {
        loadConfig()
        return config.filterNotNull()
    }

    fun getKomgaLibraries(): Flow<List<KomfMediaServerLibrary>> {
        coroutineScope.launch { loadKomgaLibraries() }
        return komgaLibraries.filterNotNull()
    }

    fun getKavitaLibraries(): Flow<List<KomfMediaServerLibrary>> {
        coroutineScope.launch { loadKavitaLibraries() }
        return kavitaLibraries.filterNotNull()
    }

    suspend fun loadConfig() {
        notifications.runCatchingToNotifications { config.value = komfConfigClient.getConfig() }
            .onFailure { configError.value = it }
            .onSuccess { configError.value = null }
    }

    suspend fun loadKomgaLibraries() {
        runCatching { komgaServerClient.getLibraries() }.onSuccess { komgaLibraries.value = it }
    }

    suspend fun loadKavitaLibraries() {
        runCatching { kavitaServerClient.getLibraries() }.onSuccess { kavitaLibraries.value = it }
    }
}
