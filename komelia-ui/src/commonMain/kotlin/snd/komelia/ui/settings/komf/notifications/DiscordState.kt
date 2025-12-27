package snd.komelia.ui.settings.komf.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
import snd.komelia.ui.settings.komf.KomfSharedState
import snd.komf.api.PatchValue.Some
import snd.komf.api.config.DiscordConfigUpdateRequest
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.config.NotificationConfigUpdateRequest
import snd.komf.api.notifications.EmbedField
import snd.komf.api.notifications.EmbedFieldTemplate
import snd.komf.api.notifications.KomfDiscordRequest
import snd.komf.api.notifications.KomfDiscordTemplates
import snd.komf.client.KomfConfigClient
import snd.komf.client.KomfNotificationClient

class DiscordState(
    private val komfConfigClient: KomfConfigClient,
    private val komfNotificationClient: KomfNotificationClient,
    private val appNotifications: AppNotifications,
    private val komfConfig: KomfSharedState,
    private val coroutineScope: CoroutineScope,
    val notificationContext: NotificationContextState,
) {
    var discordUploadSeriesCover by mutableStateOf(false)
        private set
    var discordWebhooks by mutableStateOf(emptyList<String>())
        private set

    var titleTemplate by mutableStateOf("")
    var titleUrlTemplate by mutableStateOf("")
    var descriptionTemplate by mutableStateOf("")
    var footerTemplate by mutableStateOf("")
    var fieldTemplates by mutableStateOf<List<EmbedFieldState>>(emptyList())

    var titlePreview by mutableStateOf("")
    var titleUrlPreview by mutableStateOf("")
    var descriptionPreview by mutableStateOf("")
    var fieldPreviews by mutableStateOf<List<EmbedField>>(emptyList())
    var footerPreview by mutableStateOf("")

    suspend fun initialize(config: KomfConfig) {
        discordUploadSeriesCover = config.notifications.discord.seriesCover
        discordWebhooks = config.notifications.discord.webhooks ?: emptyList()

        val templates = komfNotificationClient.getDiscordTemplates()
        titleTemplate = templates.titleTemplate ?: ""
        titleUrlTemplate = templates.titleUrlTemplate ?: ""
        descriptionTemplate = templates.descriptionTemplate ?: ""
        fieldTemplates = templates.fields.map { EmbedFieldState(it.nameTemplate, it.valueTemplate, it.inline) }
        footerTemplate = templates.footerTemplate ?: ""
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

        val discordUpdate = DiscordConfigUpdateRequest(
            webhooks = Some(mapOf(webhooks.size - 1 to webhook))
        )
        val notificationUpdate = NotificationConfigUpdateRequest(discord = Some(discordUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    fun onDiscordWebhookRemove(webhook: String) {
        val removeIndex = discordWebhooks.indexOf(webhook)
        if (removeIndex == -1) return
        this.discordWebhooks = discordWebhooks.minus(webhook)

        val discordUpdate = DiscordConfigUpdateRequest(webhooks = Some(mapOf(removeIndex to null)))
        val notificationUpdate = NotificationConfigUpdateRequest(discord = Some(discordUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    fun onTemplatesSend() {
        if (discordWebhooks.isEmpty()) {
            appNotifications.add(AppNotification.Error("No configured Discord webhooks"))
            return
        }

        appNotifications.runCatchingToNotifications(coroutineScope) {
            komfNotificationClient.sendDiscord(
                KomfDiscordRequest(
                    context = notificationContext.getKomfNotificationContext(),
                    templates = getKomfDiscordTemplates()
                )
            )
        }
    }

    fun onTemplatesSave() {
        appNotifications.runCatchingToNotifications(coroutineScope) {
            komfNotificationClient.updateDiscordTemplates(getKomfDiscordTemplates())
            appNotifications.add(AppNotification.Success("Templates Saved"))
        }

    }

    private fun onConfigUpdate(request: KomfConfigUpdateRequest) {
        coroutineScope.launch {
            appNotifications.runCatchingToNotifications { komfConfigClient.updateConfig(request) }
                .onFailure { initialize(komfConfig.getConfig().first()) }
        }
    }

    fun onTitleUrlTemplateChange(titleUrl: String) {
        titleUrlTemplate = if (titleUrl == "http://" || titleUrl == "https://") ""
        else titleUrl
    }

    fun onTemplateRender() {
        appNotifications.runCatchingToNotifications(coroutineScope) {
            val rendered = komfNotificationClient.renderDiscord(
                KomfDiscordRequest(
                    context = notificationContext.getKomfNotificationContext(),
                    templates = getKomfDiscordTemplates()
                )
            )

            titlePreview = rendered.title ?: ""
            titleUrlPreview = rendered.titleUrl ?: ""
            descriptionPreview = rendered.description ?: ""
            fieldPreviews = rendered.fields
            footerPreview = rendered.footer ?: ""

        }
    }

    private fun getKomfDiscordTemplates() = KomfDiscordTemplates(
        titleTemplate = titleTemplate.ifBlank { null },
        titleUrlTemplate = titleUrlTemplate.ifBlank { null },
        fields = fieldTemplates.map {
            EmbedFieldTemplate(
                nameTemplate = it.nameTemplate,
                valueTemplate = it.valueTemplate,
                inline = it.inline
            )
        },
        descriptionTemplate = descriptionTemplate.ifBlank { null },
        footerTemplate = footerTemplate.ifBlank { null }
    )

    fun onFieldAdd() {
        fieldTemplates = fieldTemplates + EmbedFieldState()
    }

    fun onFieldDelete(field: EmbedFieldState) {
        fieldTemplates = fieldTemplates - field
    }

    class EmbedFieldState(
        nameTemplate: String = "",
        valueTemplate: String = "",
        inline: Boolean = false
    ) {
        var nameTemplate by mutableStateOf(nameTemplate)
        var valueTemplate by mutableStateOf(valueTemplate)
        var inline by mutableStateOf(inline)
    }
}