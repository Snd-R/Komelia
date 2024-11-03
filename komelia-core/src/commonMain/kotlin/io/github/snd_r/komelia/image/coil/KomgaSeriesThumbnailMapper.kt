package io.github.snd_r.komelia.image.coil

import coil3.map.Mapper
import coil3.request.Options
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.BookThumbnail
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.CollectionThumbnail
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.ReadListThumbnail
import io.github.snd_r.komelia.ui.dialogs.PosterEditState.KomgaThumbnail.SeriesThumbnail
import kotlinx.coroutines.flow.StateFlow

class KomgaSeriesThumbnailMapper(private val serverUrl: StateFlow<String>) : Mapper<KomgaThumbnail, String> {

    override fun map(data: KomgaThumbnail, options: Options): String {
        val urlString = when (data) {
            is SeriesThumbnail -> "${serverUrl.value}/api/v1/series/${data.komgaThumbnail.seriesId}/thumbnails/${data.komgaThumbnail.id}"
            is BookThumbnail -> "${serverUrl.value}/api/v1/books/${data.komgaThumbnail.bookId}/thumbnails/${data.komgaThumbnail.id}"
            is CollectionThumbnail -> "${serverUrl.value}/api/v1/collections/${data.komgaThumbnail.collectionId}/thumbnails/${data.komgaThumbnail.id}"
            is ReadListThumbnail -> "${serverUrl.value}/api/v1/readlists/${data.komgaThumbnail.readListId}/thumbnails/${data.komgaThumbnail.id}"
        }

        return removeEmptyPathSegments(urlString)
    }
}