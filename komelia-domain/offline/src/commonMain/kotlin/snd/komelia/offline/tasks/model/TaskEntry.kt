package snd.komelia.offline.tasks.model

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import snd.komelia.offline.model.BookMetadataPatchCapability
import snd.komelia.offline.tasks.model.TaskEntry.TaskStatus.NEW
import snd.komga.client.book.KomgaBookId
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesId

const val HIGHEST_PRIORITY = 8
const val HIGH_PRIORITY = 6
const val DEFAULT_PRIORITY = 4
const val LOW_PRIORITY = 2
const val LOWEST_PRIORITY = 0


@Serializable
data class TaskEntry(
    val task: TaskData,
    val priority: Int = DEFAULT_PRIORITY,
    val status: TaskStatus = NEW,
    val uniqueName: String = task.uniqueName,
) {
    enum class TaskStatus {
        NEW,
        RUNNING,
    }
}

@Serializable
sealed interface TaskData {
    val uniqueName: String

    @Serializable
    @SerialName("ScanLibrary")
    data class ScanLibrary(
        val libraryId: KomgaLibraryId,
        val scanDeep: Boolean,
    ) : TaskData {
        override val uniqueName = "ScanLibrary_${libraryId}_deep_$scanDeep"

    }

    @Serializable
    @SerialName("EmptyTrash")
    data class EmptyTrash(val libraryId: KomgaLibraryId) : TaskData {
        override val uniqueName = "EmptyTrash_${libraryId}"
    }

    @Serializable
    @SerialName("RefreshBookMetadata")
    data class RefreshBookMetadata(
        val bookId: KomgaBookId,
        val capabilities: Set<BookMetadataPatchCapability>,
    ) : TaskData {
        override val uniqueName = "RefreshBookMetadata_${bookId}"
    }

    @Serializable
    @SerialName("RefreshSeriesMetadata")
    data class RefreshSeriesMetadata(val seriesId: KomgaSeriesId) : TaskData {
        override val uniqueName = "RefreshSeriesMetadata_${seriesId}"

    }

    @Serializable
    @SerialName("AggregateSeriesMetadata")
    data class AggregateSeriesMetadata(val seriesId: KomgaSeriesId) : TaskData {
        override val uniqueName = "AggregateSeriesMetadata_${seriesId}"
    }

    @Serializable
    @SerialName("DeleteBook")
    data class DeleteBook(val bookId: KomgaBookId) : TaskData {
        override val uniqueName = "DeleteBook_${bookId}"
    }

    @Serializable
    @SerialName("DeleteBookFiles")
    data class DeleteBookFiles(val file: PlatformFile) : TaskData {
        override val uniqueName = "DeleteBookFiles_${file}"
    }

    @Serializable
    @SerialName("DeleteSeries")
    data class DeleteSeries(val seriesId: KomgaSeriesId) : TaskData {
        override val uniqueName = "DeleteSeries_${seriesId}"
    }

    @Serializable
    @SerialName("DeleteLibrary")
    data class DeleteLibrary(val libraryId: KomgaLibraryId) : TaskData {
        override val uniqueName = "DeleteLibrary_${libraryId}"
    }

    @Serializable
    @SerialName("DownloadBook")
    data class DownloadBook(val bookId: KomgaBookId) : TaskData {
        override val uniqueName = "DownloadBook_${bookId}"
    }

    @Serializable
    @SerialName("DownloadSeries")
    data class DownloadSeries(val seriesId: KomgaSeriesId) : TaskData {
        override val uniqueName = "DownloadSeries_${seriesId}"
    }

    @Serializable
    @SerialName("DownloadBookCancel")
    data class DownloadBookCancel(val bookId: KomgaBookId) : TaskData {
        override val uniqueName = "DownloadBookCancel_${bookId}"
    }
}

