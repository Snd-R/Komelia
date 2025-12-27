package snd.komelia.offline.series.actions

import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.series.model.OfflineThumbnailSeries
import snd.komga.client.series.KomgaSeriesId

class SeriesAddThumbnailAction(
) : OfflineAction {

    suspend fun run(
        seriesId: KomgaSeriesId,
        file: ByteArray,
        selected: Boolean
    ): OfflineThumbnailSeries {
        TODO("Not yet implemented")
    }
}