package io.github.snd_r.komelia.image.coil

import coil3.map.Mapper
import coil3.request.Options
import io.github.snd_r.komelia.ui.reader.PageMetadata
import kotlinx.coroutines.flow.StateFlow

class KomgaBookPageMapper(private val serverUrl: StateFlow<String>) : Mapper<PageMetadata, String> {

    override fun map(
        data: PageMetadata,
        options: Options
    ): String {
        return "${serverUrl.value}/api/v1/books/${data.bookId}/pages/${data.pageNumber}"
    }
}