package io.github.snd_r.komelia.image

import coil3.disk.DiskCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.image.ImageSource.FilePathSource
import io.github.snd_r.komelia.image.ImageSource.MemorySource
import io.github.snd_r.komelia.image.ReaderImage.PageId
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import okio.FileSystem
import okio.Path.Companion.toPath
import snd.komelia.image.ImageDecoder
import snd.komelia.image.KomeliaImage
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId

private val logger = KotlinLogging.logger {}

class BookImageLoader(
    private val bookClient: KomgaBookClient,
    private val imageDecoder: ImageDecoder,
    private val readerImageFactory: ReaderImageFactory,
    //TODO consider non coil disk cache implementation?
    val diskCache: DiskCache?,
) {
    val fileSystem = diskCache?.fileSystem

    suspend fun loadReaderImage(bookId: KomgaBookId, page: Int): ReaderImageResult {
        return try {
            val source = doLoad(bookId, page)
            ReaderImageResult.Success(readerImageFactory.getImage(source, PageId(bookId.value, page)))
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            logger.catching(e)
            ReaderImageResult.Error(e)
        }
    }

    // TODO remove
    suspend fun loadImage(bookId: KomgaBookId, page: Int): ImageResult {
        return try {
            doLoad(bookId, page).use { source ->
                val image = when (source) {
                    is FilePathSource -> {
                        val fileSystem = checkNotNull(fileSystem)
                        imageDecoder.decode(fileSystem.read(source.path.toPath()) { readByteArray() })
                    }

                    is MemorySource -> imageDecoder.decode(source.data)
                }
                ImageResult.Success(image)
            }
        } catch (e: Throwable) {
            currentCoroutineContext().ensureActive()
            logger.catching(e)
            ImageResult.Error(e)
        }
    }

    private suspend fun doLoad(bookId: KomgaBookId, page: Int): ImageSource {
        val pageId = PageId(bookId.value, page)
        if (diskCache == null) {
            val bytes: ByteArray = bookClient.getBookPage(bookId, page)
            return MemorySource(bytes)
        }

        val existingSnapshot = diskCache.openSnapshot(pageId.toString())
        val fileSystem = diskCache.fileSystem
        if (existingSnapshot != null) {
            return FilePathSource(existingSnapshot)
        }

        val bytes = bookClient.getBookPage(bookId, page)
        val newSnapshot = writeToDiskCache(
            fileSystem = fileSystem,
            cacheKey = pageId.toString(),
            bytes = bytes
        )

        return newSnapshot?.let { FilePathSource(it) }
            ?: MemorySource(bytes)
    }

    private fun writeToDiskCache(
        fileSystem: FileSystem,
        cacheKey: String,
        bytes: ByteArray,
    ): DiskCache.Snapshot? {
        val editor = diskCache?.openEditor(cacheKey) ?: return null
        try {
            fileSystem.write(editor.data) { this.write(bytes) }
            return editor.commitAndOpenSnapshot()
        } catch (e: Exception) {
            editor.abort()
            throw e
        }
    }
}

sealed interface ReaderImageResult {
    val image: ReaderImage?

    data class Success(override val image: ReaderImage) : ReaderImageResult
    data class Error(val throwable: Throwable) : ReaderImageResult {
        override val image: ReaderImage? = null
    }
}

sealed interface ImageResult {
    val image: KomeliaImage?

    data class Success(override val image: KomeliaImage) : ImageResult
    data class Error(val throwable: Throwable) : ImageResult {
        override val image: KomeliaImage? = null
    }
}

sealed interface ImageSource : AutoCloseable {
    class MemorySource(val data: ByteArray) : ImageSource {
        override fun close() = Unit
    }

    class FilePathSource(
        val path: String,
        private val cacheLock: DiskCache.Snapshot?
    ) : ImageSource {
        constructor(snapshot: DiskCache.Snapshot) : this(path = snapshot.data.toString(), cacheLock = snapshot)

        override fun close() {
            cacheLock?.close()
        }
    }
}