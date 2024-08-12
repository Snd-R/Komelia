package io.github.snd_r.komelia.ui.settings.komf

import cafe.adriel.voyager.core.model.StateScreenModel
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import snd.komf.api.config.KomfConfig
import snd.komf.client.KomfConfigClient

class KomfConfigState(
    private val komfConfigClient: KomfConfigClient,
    private val notifications: AppNotifications
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var config: MutableStateFlow<KomfConfig?> = MutableStateFlow(null)
    private val mutableErrorFlow = MutableStateFlow<Throwable?>(null)
    val errorFlow = mutableErrorFlow.asStateFlow()

    suspend fun getConfig(): StateFlow<KomfConfig> {
        val currentConfig = config.value

        if (currentConfig == null) {
            val newConfig = try {
                komfConfigClient.getConfig()
            } catch (e: Exception) {
                mutableErrorFlow.value = e
                throw e
            }

            mutableErrorFlow.value = null
            config.value = newConfig
            return config
                .filterNotNull()
                .stateIn(scope, SharingStarted.Eagerly, newConfig)
        } else {
            scope.launch {
                notifications.runCatchingToNotifications { config.value = komfConfigClient.getConfig() }
                    .onFailure { mutableErrorFlow.value = it }
                    .onSuccess { mutableErrorFlow.value = null }
            }

            return config
                .filterNotNull()
                .stateIn(scope, SharingStarted.Eagerly, currentConfig)
        }
    }
}

