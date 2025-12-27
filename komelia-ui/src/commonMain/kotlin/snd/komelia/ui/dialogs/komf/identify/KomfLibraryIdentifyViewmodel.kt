package snd.komelia.ui.dialogs.komf.identify

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
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