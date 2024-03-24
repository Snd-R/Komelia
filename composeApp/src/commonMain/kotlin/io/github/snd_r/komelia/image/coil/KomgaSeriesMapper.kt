package io.github.snd_r.komelia.image.coil

import coil3.map.Mapper
import coil3.request.Options
import io.github.snd_r.komelia.ui.common.images.SeriesThumbnailRequest
import kotlinx.coroutines.flow.StateFlow

class KomgaSeriesMapper(private val serverUrl: StateFlow<String>) : Mapper<SeriesThumbnailRequest, String> {

    override fun map(data: SeriesThumbnailRequest, options: Options): String {
        return "${serverUrl.value}/api/v1/series/${data.seriesId}/thumbnail"
    }
}