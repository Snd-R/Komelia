package io.github.snd_r.komelia.image

import coil3.disk.DiskCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.image.ReaderImage.PageId
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okio.FileSystem
import snd.komelia.image.ImageDecoder
import snd.komelia.image.KomeliaImage
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId

private val logger = KotlinLogging.logger {}

class BookImageLoader(
    private val bookClient: KomgaBookClient,
    private val decoder: ImageDecoder,
    //TODO consider non coil disk cache implementation?
    val diskCache: DiskCache?,
) {

    suspend fun loadImage(bookId: KomgaBookId, page: Int): ImageResult {
        return try {
            val image = doLoad(bookId, page)
            ImageResult.Success(image)
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            logger.catching(e)
            ImageResult.Error(e)
        }
    }

    private suspend fun doLoad(bookId: KomgaBookId, page: Int): KomeliaImage {
        val pageId = PageId(bookId.value, page)
        if (diskCache == null) {
            val bytes: ByteArray = bookClient.getBookPage(bookId, page)
            return decoder.decode(bytes)

        }
        diskCache.openSnapshot(pageId.toString()).use { snapshot ->
            val fileSystem = diskCache.fileSystem
            if (snapshot != null) {
                return decoder.decodeFromFile(snapshot.data.toString())
            }

            val bytes = bookClient.getBookPage(bookId, page)
            writeToDiskCache(
                fileSystem = fileSystem,
                cacheKey = pageId.toString(),
                bytes = bytes
            )

            return decoder.decode(bytes)
        }
    }

    private fun writeToDiskCache(
        fileSystem: FileSystem,
        cacheKey: String,
        bytes: ByteArray,
    ) {
        val editor = diskCache?.openEditor(cacheKey) ?: return
        try {
            fileSystem.write(editor.data) { this.write(bytes) }
            editor.commit()
        } catch (e: Exception) {
            editor.abort()
            throw e
        }
    }
}

sealed interface ImageResult {
    val image: KomeliaImage?

    data class Success(override val image: KomeliaImage) : ImageResult
    data class Error(val throwable: Throwable) : ImageResult {
        override val image: KomeliaImage? = null
    }
}
