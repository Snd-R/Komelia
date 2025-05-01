package io.github.snd_r.komelia.ui.dialogs.komf.reset

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.snd_r.komelia.AppNotifications
import snd.komf.api.KomfServerLibraryId
import snd.komf.api.KomfServerSeriesId
import snd.komf.client.KomfMetadataClient

class KomfResetMetadataDialogViewModel(
    private val appNotifications: AppNotifications,
    private val komfMetadataClient: KomfMetadataClient,
    private val onDismiss: () -> Unit,
) {

    var removeComicInfo by mutableStateOf(false)

    suspend fun onSeriesReset(
        seriesId: KomfServerSeriesId,
        libraryId: KomfServerLibraryId,
    ) {
        appNotifications.runCatchingToNotifications {
            komfMetadataClient.resetSeries(
                libraryId = libraryId,
                seriesId = seriesId,
                removeComicInfo = removeComicInfo
            )
        }.onFailure { onDismiss() }
    }

    suspend fun onLibraryReset(libraryId: KomfServerLibraryId) {
        appNotifications.runCatchingToNotifications {
            komfMetadataClient.resetLibrary(
                libraryId = libraryId,
                removeComicInfo = removeComicInfo
            )
        }.onFailure { onDismiss() }
    }
}