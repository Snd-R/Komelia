package io.github.snd_r.komelia.image.coil

import coil3.map.Mapper
import coil3.request.Options
import io.github.snd_r.komelia.ui.common.images.BookThumbnailRequest
import kotlinx.coroutines.flow.StateFlow

class KomgaBookMapper(private val serverUrl: StateFlow<String>) : Mapper<BookThumbnailRequest, String> {

    override fun map(data: BookThumbnailRequest, options: Options): String {
        return "${serverUrl.value}/api/v1/books/${data.bookId}/thumbnail"
    }
}