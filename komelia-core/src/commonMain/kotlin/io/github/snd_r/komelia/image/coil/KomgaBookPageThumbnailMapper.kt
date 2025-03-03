package io.github.snd_r.komelia.image.coil

import coil3.map.Mapper
import coil3.request.Options
import io.github.snd_r.komelia.ui.reader.image.common.BookPageThumbnailRequest
import kotlinx.coroutines.flow.StateFlow

class KomgaBookPageThumbnailMapper(private val serverUrl: StateFlow<String>) :
    Mapper<BookPageThumbnailRequest, String> {

    override fun map(
        data: BookPageThumbnailRequest,
        options: Options
    ): String {
        return removeEmptyPathSegments("${serverUrl.value}/api/v1/books/${data.bookId}/pages/${data.pageNumber}/thumbnail")
    }
}