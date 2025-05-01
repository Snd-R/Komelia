package io.github.snd_r.komelia.ui.dialogs.komf.identify

import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import snd.komf.api.KomfServerLibraryId
import snd.komf.client.KomfMetadataClient

class KomfLibraryIdentifyViewmodel(
    private val libraryId: KomfServerLibraryId,
    private val appNotifications: AppNotifications,
    private val komfMetadataClient: KomfMetadataClient,
) {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    fun autoIdentify() {
        appNotifications.runCatchingToNotifications(scope) {
            komfMetadataClient.matchLibrary(libraryId)
            appNotifications.add(AppNotification.Normal("Launched library auto-identification"))
        }
    }
}