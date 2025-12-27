package snd.komelia.offline.series.actions

import snd.komelia.offline.action.OfflineAction
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.series.KomgaSeriesId

class SeriesSelectThumbnailAction(
) : OfflineAction {

    suspend fun run(
        seriesId: KomgaSeriesId,
        thumbnailId: KomgaThumbnailId
    ) {
        TODO("Not yet implemented")
    }
}