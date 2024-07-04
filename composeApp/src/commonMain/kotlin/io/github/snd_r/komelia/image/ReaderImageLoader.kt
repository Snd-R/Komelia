package io.github.snd_r.komelia.image

import coil3.disk.DiskCache
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.ImageResult
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookId
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import okio.FileSystem
import okio.Path
import kotlin.random.Random
import kotlin.random.nextULong

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
        val cacheKey = "${bookId.value}_$page"
        if (diskCache != null) {
            var snapshot = diskCache.openSnapshot(cacheKey)
            val fileSystem = diskCache.fileSystem
            try {
                if (snapshot != null) {
                    return decoder.decode(snapshot.data)
                }

                val result = bookClient.streamBookPage(bookId, page) { response ->
                    val newSnapshot = writeToDiskCache(fileSystem, snapshot, cacheKey, response)
                    snapshot = newSnapshot

                    if (newSnapshot != null) {
                        decoder.decode(newSnapshot.data)
                    } else {
                        decoder.decode(response.body<ByteArray>())
                    }
                }

                return result

            } catch (e: Exception) {
                snapshot?.close()
                throw e
            }
        }

        val bytes: ByteArray = bookClient.getBookPage(bookId, page)
        return decoder.decode(bytes)
    }

    private suspend fun writeToDiskCache(
        fileSystem: FileSystem,
        snapshot: DiskCache.Snapshot?,
        cacheKey: String,
        response: HttpResponse,
    ): DiskCache.Snapshot? {
        // Open a new editor.
        val editor = if (snapshot != null) {
            snapshot.closeAndOpenEditor()
        } else {
            diskCache?.openEditor(cacheKey)
        }
        if (editor == null) return null

        try {
            val channel = response.bodyAsChannel()
            fileSystem.write(editor.data) {
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining((8 * 1024).toLong())
                    while (!packet.isEmpty) {
                        val bytes = packet.readBytes()
                        this.write(bytes)
                    }
                }
            }

            return editor.commitAndOpenSnapshot()
        } catch (e: Exception) {
            editor.abort()
            throw e
        }
    }
}

interface ImageDecoder {
    fun decode(bytes: ByteArray): ReaderImage
    fun decode(cacheFile: Path): ReaderImage
}

internal fun FileSystem.createFile(file: Path, mustCreate: Boolean = false) {
    if (mustCreate) {
        sink(file, mustCreate = true).close()
    } else if (!exists(file)) {
        sink(file).close()
    }
}

internal fun FileSystem.createTempFile(): Path {
    var tempFile: Path
    do {
        tempFile = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "tmp_${Random.nextULong()}"
    } while (exists(tempFile))
    createFile(tempFile, mustCreate = true)
    return tempFile
}