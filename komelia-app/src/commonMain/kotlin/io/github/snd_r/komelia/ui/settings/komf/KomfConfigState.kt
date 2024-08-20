package io.github.snd_r.komelia.ui.settings.komf

import io.github.snd_r.komelia.AppNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import snd.komf.api.config.KomfConfig
import snd.komf.client.KomfConfigClient

class KomfConfigState(
    private val komfConfigClient: KomfConfigClient,
    private val notifications: AppNotifications
) {
    private var config: MutableStateFlow<KomfConfig?> = MutableStateFlow(null)
    private val mutableErrorFlow = MutableStateFlow<Throwable?>(null)
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val errorFlow = mutableErrorFlow.asStateFlow()

    fun getConfig(): Flow<KomfConfig> {
        val flow = config.filterNotNull()

        coroutineScope.launch {
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
            } else {
                notifications.runCatchingToNotifications { config.value = komfConfigClient.getConfig() }
                    .onFailure { mutableErrorFlow.value = it }
                    .onSuccess { mutableErrorFlow.value = null }
            }
        }

        return flow
    }
}

