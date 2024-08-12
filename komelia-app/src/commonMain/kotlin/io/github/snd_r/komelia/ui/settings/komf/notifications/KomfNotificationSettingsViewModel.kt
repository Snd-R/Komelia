package io.github.snd_r.komelia.ui.settings.komf.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.settings.komf.KomfConfigState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komf.api.PatchValue.Some
import snd.komf.api.config.DiscordConfigUpdateRequest
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.config.NotificationConfigUpdateRequest
import snd.komf.client.KomfConfigClient
import snd.komga.client.library.KomgaLibrary

class KomfNotificationSettingsViewModel(
    private val komfConfigClient: KomfConfigClient,
    private val appNotifications: AppNotifications,
    val komfConfig: KomfConfigState,
    val libraries: StateFlow<List<KomgaLibrary>>,
) : StateScreenModel<LoadState<Unit>>(LoadState.Uninitialized) {

    var discordUploadSeriesCover by mutableStateOf(false)
        private set
    var discordWebhooks by mutableStateOf(emptyList<String>())
        private set

    suspend fun initialize() {
        appNotifications.runCatchingToNotifications { komfConfig.getConfig() }
            .onFailure { mutableState.value = LoadState.Error(it) }
            .onSuccess { config ->
                mutableState.value = LoadState.Success(Unit)
                config.onEach { initFields(it) }.launchIn(screenModelScope)
            }
    }

    private fun initFields(config: KomfConfig) {
        discordUploadSeriesCover = config.notifications.discord.seriesCover
        discordWebhooks = config.notifications.discord.webhooks ?: emptyList()
    }

    fun onSeriesCoverChange(seriesCover: Boolean) {
        discordUploadSeriesCover = seriesCover
        val discordUpdate = DiscordConfigUpdateRequest(seriesCover = Some(seriesCover))
        val notificationUpdate = NotificationConfigUpdateRequest(discord = Some(discordUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    fun onDiscordWebhookAdd(webhook: String) {
        val webhooks = discordWebhooks.plus(webhook)
        this.discordWebhooks = webhooks

        val discordUpdate = DiscordConfigUpdateRequest(webhooks = Some(webhooks))
        val notificationUpdate = NotificationConfigUpdateRequest(discord = Some(discordUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    fun onDiscordWebhookRemove(webhook: String) {
        val webhooks = discordWebhooks.minus(webhook)
        this.discordWebhooks = webhooks

        val discordUpdate = DiscordConfigUpdateRequest(webhooks = Some(webhooks))
        val notificationUpdate = NotificationConfigUpdateRequest(discord = Some(discordUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    private fun onConfigUpdate(request: KomfConfigUpdateRequest) {
        screenModelScope.launch {
            appNotifications.runCatchingToNotifications { komfConfigClient.updateConfig(request) }
                .onFailure { mutableState.value = LoadState.Error(it) }

        }
    }
}