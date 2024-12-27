package io.github.snd_r.komelia.image

import coil3.disk.DiskCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.ImageResult
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okio.FileSystem
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId

private val logger = KotlinLogging.logger {}

class ReaderImageLoader(
    private val bookClient: KomgaBookClient,
    private val decoder: ReaderImageFactory,
    //TODO consider non coil disk cache implementation?
    private val diskCache: DiskCache?,
) {
    suspend fun load(bookId: KomgaBookId, page: Int): ImageResult {
        return try {
            ImageResult.Success(doLoad(bookId, page))
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            logger.catching(e)
            ImageResult.Error(e)
        }
    }

    private suspend fun doLoad(bookId: KomgaBookId, page: Int): ReaderImage {
        val pageId = PageId(bookId.value, page)
        if (diskCache == null) {
            val bytes: ByteArray = bookClient.getBookPage(bookId, page)
            return decoder.getImage(bytes, pageId)

        }
        diskCache.openSnapshot(pageId.toString()).use { snapshot ->
            val fileSystem = diskCache.fileSystem
            if (snapshot != null) {
                return decoder.getImage(snapshot.data, pageId)
            }

            val bytes = bookClient.getBookPage(bookId, page)
            writeToDiskCache(
                fileSystem = fileSystem,
                cacheKey = pageId.toString(),
                bytes = bytes
            )

            return decoder.getImage(bytes, pageId)
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

