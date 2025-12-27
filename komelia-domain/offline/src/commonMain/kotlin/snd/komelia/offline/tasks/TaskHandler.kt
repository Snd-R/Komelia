package snd.komelia.offline.tasks

import io.github.oshai.kotlinlogging.KotlinLogging
import snd.komelia.offline.action.OfflineActions
import snd.komelia.offline.book.actions.BookDeleteAction
import snd.komelia.offline.book.actions.BookDeleteFilesAction
import snd.komelia.offline.book.actions.BookMetadataRefreshAction
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.library.actions.LibraryDeleteAction
import snd.komelia.offline.library.actions.LibraryEmptyTrashAction
import snd.komelia.offline.series.actions.SeriesAggregateBookMetadataAction
import snd.komelia.offline.series.actions.SeriesDeleteAction
import snd.komelia.offline.series.actions.SeriesRefreshMetadataAction
import snd.komelia.offline.sync.PlatformDownloadManager
import snd.komelia.offline.tasks.model.TaskData
import snd.komelia.offline.tasks.model.TaskData.AggregateSeriesMetadata
import snd.komelia.offline.tasks.model.TaskData.DeleteBook
import snd.komelia.offline.tasks.model.TaskData.DeleteLibrary
import snd.komelia.offline.tasks.model.TaskData.DeleteSeries
import snd.komelia.offline.tasks.model.TaskData.DownloadBook
import snd.komelia.offline.tasks.model.TaskData.DownloadSeries
import snd.komelia.offline.tasks.model.TaskData.EmptyTrash
import snd.komelia.offline.tasks.model.TaskData.RefreshBookMetadata
import snd.komelia.offline.tasks.model.TaskData.RefreshSeriesMetadata
import snd.komelia.offline.tasks.model.TaskData.ScanLibrary
import snd.komelia.offline.tasks.model.TaskEntry
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.search.anyOfBooks

private val logger = KotlinLogging.logger { }

class TaskHandler(
    private val actions: OfflineActions,
    private val bookRepository: OfflineBookRepository,
    private val taskEmitter: OfflineTaskEmitter,
    private val downloadManager: PlatformDownloadManager,
    private val komgaBookClient: KomgaBookClient,
) {
    suspend fun handleTask(entry: TaskEntry) {
        logger.info { "handling task ${entry.task}" }
        when (val task = entry.task) {
            is AggregateSeriesMetadata -> actions.get<SeriesAggregateBookMetadataAction>().execute(task.seriesId)
            is DeleteBook -> {
                bookRepository.find(task.bookId)?.let { book ->
                    if (book.oneshot) {
                        actions.get<SeriesDeleteAction>().execute(book.seriesId)
                    } else {
                        actions.get<BookDeleteAction>().execute(book.id)
                    }
                }
            }

            is DeleteSeries -> {
                actions.get<SeriesDeleteAction>().execute(task.seriesId)
            }

            is DeleteLibrary -> {
                actions.get<LibraryDeleteAction>().execute(task.libraryId)
            }

            is EmptyTrash -> actions.get<LibraryEmptyTrashAction>().execute(task.libraryId)
            is RefreshBookMetadata -> {
                bookRepository.find(task.bookId)?.let { book ->
                    actions.get<BookMetadataRefreshAction>().run(task.bookId)
                    taskEmitter.refreshSeriesMetadata(book.seriesId)
                }
            }

            is RefreshSeriesMetadata -> {
                actions.get<SeriesRefreshMetadataAction>().run(task.seriesId)
                taskEmitter.aggregateSeriesMetadata(task.seriesId)
            }

            is ScanLibrary -> {}

            is DownloadBook -> {
                downloadManager.launchBookDownload(task.bookId)
            }

            is DownloadSeries -> {
                val books = komgaBookClient.getBookList(
                    conditionBuilder = anyOfBooks { seriesId { isEqualTo(task.seriesId) } },
                    pageRequest = KomgaPageRequest(unpaged = true)
                ).content

                books.forEach { taskEmitter.downloadBook(it.id) }
            }

            is TaskData.DownloadBookCancel -> downloadManager.cancelBookDownload(task.bookId)
            is TaskData.DeleteBookFiles -> actions.get<BookDeleteFilesAction>().execute(task.file)
        }
    }
}