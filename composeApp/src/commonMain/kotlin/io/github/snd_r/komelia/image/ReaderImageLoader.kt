package io.github.snd_r.komelia.image

import coil3.disk.DiskCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ImageResult
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookId
import okio.FileSystem
import okio.Path

private val logger = KotlinLogging.logger {}

class ReaderImageLoader(
    private val bookClient: KomgaBookClient,
    private val decoder: ImageDecoder,
    //TODO consider non coil disk cache implementation?
    private val diskCache: DiskCache?,
) {
    suspend fun load(bookId: KomgaBookId, page: Int): ImageResult {
        return try {
            ImageResult.Success(doLoad(bookId, page))
        } catch (e: Exception) {
            logger.catching(e)
            ImageResult.Error(e)
        }
    }

    private suspend fun doLoad(bookId: KomgaBookId, page: Int): ReaderImage {
        val pageId = PageId(bookId.value, page)
        if (diskCache != null) {
            var snapshot = diskCache.openSnapshot(pageId.toString())
            val fileSystem = diskCache.fileSystem
            try {
                if (snapshot != null) {
                    return decoder.decode(snapshot.data, pageId)
                }

                val bytes = bookClient.getBookPage(bookId, page)
                val newSnapshot = writeToDiskCache(
                    fileSystem = fileSystem,
                    snapshot = snapshot,
                    cacheKey = pageId.toString(),
                    bytes = bytes
                )
                snapshot = newSnapshot

                return decoder.decode(bytes, pageId)
            } catch (e: Exception) {
                snapshot?.close()
                throw e
            }
        }

        val bytes: ByteArray = bookClient.getBookPage(bookId, page)
        return decoder.decode(bytes, pageId)
    }

    private fun writeToDiskCache(
        fileSystem: FileSystem,
        snapshot: DiskCache.Snapshot?,
        cacheKey: String,
        bytes: ByteArray,
    ): DiskCache.Snapshot? {
        // Open a new editor.
        val editor = if (snapshot != null) {
            snapshot.closeAndOpenEditor()
        } else {
            diskCache?.openEditor(cacheKey)
        }
        if (editor == null) return null

        try {
            fileSystem.write(editor.data) { this.write(bytes) }
            return editor.commitAndOpenSnapshot()
        } catch (e: Exception) {
            editor.abort()
            throw e
        }
    }
}

interface ImageDecoder {
    fun decode(bytes: ByteArray, pageId: PageId): ReaderImage
    fun decode(cacheFile: Path, pageId: PageId): ReaderImage
}
