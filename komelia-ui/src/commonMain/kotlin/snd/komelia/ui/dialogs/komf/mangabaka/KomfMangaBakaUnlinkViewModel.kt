package snd.komelia.ui.dialogs.komf.mangabaka

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import snd.komelia.AppNotifications
import snd.komelia.ManagedKomgaEvents
import snd.komelia.komga.api.model.KomeliaSeries
import snd.komf.api.KomfServerSeriesId
import snd.komf.client.KomfMangaBakaClient
import snd.komga.client.sse.KomgaEvent

class KomfMangaBakaUnlinkViewModel(
    val series: KomeliaSeries,
    private val mangaBakaClient: KomfMangaBakaClient,
    private val appNotifications: AppNotifications,
    private val appEvents: ManagedKomgaEvents,
    private val onDismiss: () -> Unit,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun onDispose() {
        coroutineScope.cancel()
    }

    fun onUnlink() {
        appNotifications.runCatchingToNotifications(coroutineScope) {
            mangaBakaClient.unlink(KomfServerSeriesId(this.series.id.value))
            appEvents.emitEvent(KomgaEvent.SeriesChanged(series.id, series.libraryId))
            onDismiss()
        }
    }
}