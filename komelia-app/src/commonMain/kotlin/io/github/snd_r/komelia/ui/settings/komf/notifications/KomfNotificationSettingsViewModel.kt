package io.github.snd_r.komelia.ui.settings.komf.notifications

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.settings.komf.KomfConfigState
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import snd.komf.client.KomfConfigClient
import snd.komf.client.KomfNotificationClient

class KomfNotificationSettingsViewModel(
    komfConfigClient: KomfConfigClient,
    komfNotificationClient: KomfNotificationClient,
    private val appNotifications: AppNotifications,
    val komfConfig: KomfConfigState,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    private val notificationContext = NotificationContextState()

    val discordState = DiscordState(
        komfConfigClient = komfConfigClient,
        komfNotificationClient = komfNotificationClient,
        appNotifications = appNotifications,
        notificationContext = notificationContext,
        komfConfig = komfConfig,
        coroutineScope = screenModelScope
    )

    val appriseState = AppriseState(
        komfConfigClient = komfConfigClient,
        komfNotificationClient = komfNotificationClient,
        appNotifications = appNotifications,
        notificationContext = notificationContext,
        komfConfig = komfConfig,
        coroutineScope = screenModelScope
    )

    suspend fun initialize() {
        appNotifications.runCatchingToNotifications {
            val config = komfConfig.getConfig()
            val currentConfig = config.first()
            discordState.initialize(currentConfig)
            appriseState.initialize(currentConfig)

            config.drop(1).onEach {
                discordState.initialize(it)
                appriseState.initialize(it)
            }.launchIn(screenModelScope)

        }
            .onFailure { mutableState.value = LoadState.Error(it) }
            .onSuccess { mutableState.value = LoadState.Success(Unit) }
    }
}