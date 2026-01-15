package snd.komelia.offline.sync

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.io.Sink
import snd.komelia.offline.book.actions.BookKomgaImportAction
import snd.komelia.offline.library.actions.LibraryKomgaImportAction
import snd.komelia.offline.series.actions.SeriesKomgaImportAction
import snd.komelia.offline.server.actions.MediaServerSaveAction
import snd.komelia.offline.sync.model.DownloadEvent
import snd.komelia.offline.sync.model.DownloadEvent.BookDownloadProgress
import snd.komelia.offline.user.actions.UserKomgaImportAction
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.library.KomgaLibraryClient
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.user.KomgaUserClient


private val logger = KotlinLogging.logger { }
private const val DEFAULT_BUFFER_SIZE: Int = 64 * 1024

class BookDownloadService(
    private val libraryDownloadPath: Flow<PlatformFile>,

    private val bookClient: KomgaBookClient,
    private val seriesClient: KomgaSeriesClient,
    private val libraryClient: KomgaLibraryClient,
    private val userClient: KomgaUserClient,

    private val saveUserAction: UserKomgaImportAction,
    private val saveServerAction: MediaServerSaveAction,
    private val libraryImportAction: LibraryKomgaImportAction,
    private val seriesImportAction: SeriesKomgaImportAction,
    private val bookImportAction: BookKomgaImportAction,

    private val onlineServerUrl: StateFlow<String>,
) {

    fun downloadBook(bookId: KomgaBookId): Flow<DownloadEvent> {
        return flow {
            val book = bookClient.getOne(bookId)
            try {
                val user = userClient.getMe()
                val serverUrl = onlineServerUrl.value
                val library = libraryClient.getLibrary(book.libraryId)
                val series = seriesClient.getOneSeries(book.seriesId)

                val bookFile = doDownload(book)

                val offlineServer = saveServerAction.execute(serverUrl)
                val offlineUser = saveUserAction.execute(user, offlineServer.id)
                libraryImportAction.execute(library, offlineServer.id)
                seriesImportAction.execute(series)
                bookImportAction.execute(
                    book = book,
                    offlinePath = bookFile,
                    userId = offlineUser.id,
                    localFileModifiedDate = book.fileLastModified
                )
            } catch (e: Exception) {
                currentCoroutineContext().ensureActive()
                logger.catching(e)
                emit(
                    DownloadEvent.BookDownloadError(
                        bookId = bookId,
                        error = e,
                        book = book
                    )
                )
            }
        }
    }

    private suspend fun FlowCollector<DownloadEvent>.doDownload(book: KomgaBook): PlatformFile {
        val (file, output) = prepareOutput(book, libraryDownloadPath.first())
        try {
            bookClient.getBookFile(book.id) { response ->
                val length = response.headers["Content-Length"]?.toLong() ?: 0L
                emit(BookDownloadProgress(book, length, 0))
                val channel = response.bodyAsChannel().counted()

                while (!channel.isClosedForRead) {
                    output.writePacket(channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong()))
                    emit(BookDownloadProgress(book, length, channel.totalBytesRead))
                }
            }
        } catch (e: Exception) {
            deleteFile(file)
            throw e
        } finally {
            output.close()
        }

        val event = DownloadEvent.BookDownloadCompleted(book)
        emit(event)

        return file
    }
}

internal expect suspend fun prepareOutput(book: KomgaBook, downloadPath: PlatformFile): Pair<PlatformFile, Sink>
internal expect suspend fun deleteFile(file: PlatformFile)