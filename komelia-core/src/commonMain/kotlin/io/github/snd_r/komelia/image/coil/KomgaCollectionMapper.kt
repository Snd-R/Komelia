package io.github.snd_r.komelia.image.coil

import coil3.map.Mapper
import coil3.request.Options
import io.github.snd_r.komelia.ui.common.images.CollectionThumbnailRequest
import kotlinx.coroutines.flow.StateFlow

class KomgaCollectionMapper(private val serverUrl: StateFlow<String>) : Mapper<CollectionThumbnailRequest, String> {

    override fun map(data: CollectionThumbnailRequest, options: Options): String {
        return removeEmptyPathSegments("${serverUrl.value}/api/v1/collections/${data.collectionId}/thumbnail")
    }
}