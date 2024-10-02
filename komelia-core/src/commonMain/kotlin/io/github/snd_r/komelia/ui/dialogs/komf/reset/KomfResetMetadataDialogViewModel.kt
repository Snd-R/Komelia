package io.github.snd_r.komelia.ui.dialogs.komf.reset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.client.KomfMetadataClient
import snd.komga.client.series.KomgaSeries

class KomfResetMetadataDialogViewModel(
    private val series: KomgaSeries,
    private val appNotifications: AppNotifications,
    private val komfMetadataClient: KomfMetadataClient,
    private val onDismiss: () -> Unit,
) {

    var removeComicInfo by mutableStateOf(false)

    suspend fun onReset() {
        appNotifications.runCatchingToNotifications() {
            komfMetadataClient.resetSeries(
                libraryId = KomfServerLibraryId(series.libraryId.value),
                seriesId = KomfServerSeriesId(series.id.value),
                removeComicInfo = removeComicInfo
            )
        }.onFailure { onDismiss() }
    }
}