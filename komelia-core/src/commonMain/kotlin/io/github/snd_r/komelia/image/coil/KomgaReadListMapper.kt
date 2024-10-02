package io.github.snd_r.komelia.image.coil

import coil3.map.Mapper
import coil3.request.Options
import io.github.snd_r.komelia.ui.common.images.ReadListThumbnailRequest
import kotlinx.coroutines.flow.StateFlow

class KomgaReadListMapper(private val serverUrl: StateFlow<String>) : Mapper<ReadListThumbnailRequest, String> {

    override fun map(data: ReadListThumbnailRequest, options: Options): String {
        return "${serverUrl.value}/api/v1/readlists/${data.readListId}/thumbnail"
    }
}