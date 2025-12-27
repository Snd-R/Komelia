package snd.komelia.offline.tasks

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableSharedFlow
import snd.komelia.offline.book.model.OfflineBook
import snd.komelia.offline.model.BookMetadataPatchCapability
import snd.komelia.offline.tasks.model.DEFAULT_PRIORITY
import snd.komelia.offline.tasks.model.TaskAddedEvent
import snd.komelia.offline.tasks.model.TaskData
import snd.komelia.offline.tasks.model.TaskData.AggregateSeriesMetadata
import snd.komelia.offline.tasks.model.TaskData.DeleteBook
import snd.komelia.offline.tasks.model.TaskData.DeleteSeries
import snd.komelia.offline.tasks.model.TaskData.DownloadBook
import snd.komelia.offline.tasks.model.TaskData.DownloadSeries
import snd.komelia.offline.tasks.model.TaskData.RefreshBookMetadata
import snd.komelia.offline.tasks.model.TaskData.RefreshSeriesMetadata
import snd.komelia.offline.tasks.model.TaskEntry
import snd.komelia.offline.tasks.repository.OfflineTasksRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesId

class OfflineTaskEmitter(
    private val tasksRepository: OfflineTasksRepository,
    private val tasksFlow: MutableSharedFlow<TaskAddedEvent>
) {

    suspend fun scanLibrary(
        libraryId: KomgaLibraryId,
        scanDeep: Boolean = false,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = TaskData.ScanLibrary(libraryId, scanDeep)
            )
        )
    }

    suspend fun emptyTrash(
        libraryId: KomgaLibraryId,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = TaskData.EmptyTrash(libraryId),
            )
        )
    }

    suspend fun refreshBookMetadata(
        book: OfflineBook,
        capabilities: Set<BookMetadataPatchCapability> = BookMetadataPatchCapability.entries.toSet(),
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = RefreshBookMetadata(book.id, capabilities),
            )
        )
    }

    suspend fun refreshBookMetadata(
        books: Collection<OfflineBook>,
        capabilities: Set<BookMetadataPatchCapability> = BookMetadataPatchCapability.entries.toSet(),
        priority: Int = DEFAULT_PRIORITY,
    ) {
        books.map {
            TaskEntry(
                priority = priority,
                task = RefreshBookMetadata(it.id, capabilities)
            )
        }.let { submitTasks(it) }
    }

    suspend fun refreshSeriesMetadata(
        seriesId: KomgaSeriesId,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = RefreshSeriesMetadata(seriesId),
            )
        )
    }

    suspend fun aggregateSeriesMetadata(
        seriesId: KomgaSeriesId,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = AggregateSeriesMetadata(seriesId),
            )
        )
    }

    suspend fun deleteBook(
        bookId: KomgaBookId,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = DeleteBook(bookId),
            )
        )
    }

    suspend fun deleteBookFiles(
        file: PlatformFile,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = TaskData.DeleteBookFiles(file),
            )
        )
    }

    suspend fun deleteSeries(
        seriesId: KomgaSeriesId,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = DeleteSeries(seriesId),
            )
        )
    }

    suspend fun deleteLibrary(
        libraryId: KomgaLibraryId,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = TaskData.DeleteLibrary(libraryId),
            )
        )
    }

    suspend fun downloadBook(
        bookId: KomgaBookId,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = DownloadBook(bookId),
            )
        )
    }

    suspend fun downloadSeries(
        seriesId: KomgaSeriesId,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = DownloadSeries(seriesId),
            )
        )
    }

    suspend fun cancelBookDownload(
        bookId: KomgaBookId,
        priority: Int = DEFAULT_PRIORITY,
    ) {
        submitTask(
            TaskEntry(
                priority = priority,
                task = TaskData.DownloadBookCancel(bookId),
            )
        )
    }

    private suspend fun submitTask(entry: TaskEntry) {
        tasksRepository.save(entry)
        tasksFlow.emit(TaskAddedEvent)
    }

    private suspend fun submitTasks(entries: Collection<TaskEntry>) {
        tasksRepository.save(entries)
        entries.forEach { _ -> tasksFlow.emit(TaskAddedEvent) }
    }

}