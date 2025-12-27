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
import snd.komf.api.config.AppriseConfigUpdateRequest
import snd.komf.api.config.KomfConfig
import snd.komf.api.config.KomfConfigUpdateRequest
import snd.komf.api.config.NotificationConfigUpdateRequest
import snd.komf.api.notifications.KomfAppriseRequest
import snd.komf.api.notifications.KomfAppriseTemplates
import snd.komf.client.KomfConfigClient
import snd.komf.client.KomfNotificationClient

class AppriseState(
    private val komfConfigClient: KomfConfigClient,
    private val komfNotificationClient: KomfNotificationClient,
    private val appNotifications: AppNotifications,
    private val komfConfig: KomfSharedState,
    private val coroutineScope: CoroutineScope,
    val notificationContext: NotificationContextState,
) {

    var appriseUrls by mutableStateOf(emptyList<String>())
        private set
    var uploadSeriesCover by mutableStateOf(false)
        private set
    var titleTemplate by mutableStateOf("")
    var bodyTemplate by mutableStateOf("")

    suspend fun initialize(config: KomfConfig) {
        appriseUrls = config.notifications.apprise.urls ?: emptyList()
        val templates = komfNotificationClient.getAppriseTemplates()
        titleTemplate = templates.titleTemplate ?: ""
        bodyTemplate = templates.bodyTemplate ?: ""
    }

    fun onUrlAdd(url: String) {
        val urls = appriseUrls.plus(url)
        this.appriseUrls = urls

        val appriseUpdate = AppriseConfigUpdateRequest(urls = Some(mapOf(urls.size - 1 to url)))
        val notificationUpdate = NotificationConfigUpdateRequest(apprise = Some(appriseUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    fun onUrlRemove(url: String) {
        val removeIndex = appriseUrls.indexOf(url)
        if (removeIndex == -1) return

        this.appriseUrls = appriseUrls.minus(url)

        val appriseUpdate = AppriseConfigUpdateRequest(urls = Some(mapOf(removeIndex to null)))
        val notificationUpdate = NotificationConfigUpdateRequest(apprise = Some(appriseUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    fun onSeriesCoverChange(seriesCover: Boolean) {
        uploadSeriesCover = seriesCover
        val appriseUpdate = AppriseConfigUpdateRequest(seriesCover = Some(seriesCover))
        val notificationUpdate = NotificationConfigUpdateRequest(apprise = Some(appriseUpdate))
        onConfigUpdate(KomfConfigUpdateRequest(notifications = Some(notificationUpdate)))
    }

    fun onTemplatesSend() {
        if (appriseUrls.isEmpty()) {
            appNotifications.add(AppNotification.Error("No configured Apprise urls"))
            return
        }

        appNotifications.runCatchingToNotifications(coroutineScope) {
            komfNotificationClient.sendApprise(
                KomfAppriseRequest(
                    context = notificationContext.getKomfNotificationContext(),
                    templates = KomfAppriseTemplates(
                        titleTemplate = titleTemplate.ifBlank { null },
                        bodyTemplate = bodyTemplate.ifBlank { null }
                    )
                )
            )
        }
    }

    fun onTemplatesSave() {
        appNotifications.runCatchingToNotifications(coroutineScope) {
            komfNotificationClient.updateAppriseTemplates(
                KomfAppriseTemplates(
                    titleTemplate = titleTemplate.ifBlank { null },
                    bodyTemplate = bodyTemplate.ifBlank { null }
                )
            )
            appNotifications.add(AppNotification.Success("Templates Saved"))
        }

    }

    private fun onConfigUpdate(request: KomfConfigUpdateRequest) {
        coroutineScope.launch {
            appNotifications.runCatchingToNotifications { komfConfigClient.updateConfig(request) }
                .onFailure { initialize(komfConfig.getConfig().first()) }
        }
    }
}