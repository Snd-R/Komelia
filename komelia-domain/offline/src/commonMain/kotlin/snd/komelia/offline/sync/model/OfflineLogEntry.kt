package snd.komelia.offline.sync.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import snd.komelia.offline.sync.repository.LogJournalRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.series.KomgaSeriesId
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@JvmInline
value class LogEntryId(val value: Uuid)

data class OfflineLogEntry(
    val id: LogEntryId = LogEntryId(Uuid.generateV4()),
    val message: String,
    val type: Type,
    val timestamp: Instant = Clock.System.now(),
) {
    companion object {
        internal suspend fun LogJournalRepository.logInfo(message: () -> String) {
            val entry = OfflineLogEntry(
                message = message(),
                type = Type.INFO
            )
            this.save(entry)
        }

        internal suspend fun LogJournalRepository.logError(error: Throwable?, message: () -> String) {
            val entry = OfflineLogEntry(
                message = buildString {
                    append(message())
                    error?.let {
                        append("\n")
                        append("${it::class.simpleName}: ${it.message}")
                    }
                },
                type = Type.ERROR
            )

            this.save(entry)
        }

        internal fun infoLogEntry(message: () -> String): OfflineLogEntry {
            return OfflineLogEntry(
                message = message(),
                type = Type.INFO
            )
        }

        internal fun errorLogEntry(e: Throwable, message: () -> String?): OfflineLogEntry {
            return OfflineLogEntry(
                message = buildString {
                    message()?.let {
                        append(it)
                        append("\n")
                    }
                    append("${e::class.simpleName}: ${e.message}")
                },
                type = Type.ERROR
            )
        }
    }

    enum class Type {
        DEBUG,
        INFO,
        ERROR,
    }

//    companion object {
//        fun seriesImportEntry(series: KomgaSeries, error: Throwable? = null): OfflineLogEntry {
//            return OfflineLogEntry(
//                title = "Series Import",
//                description = series.metadata.title,
//                errorMessage = error?.let { e -> "${e::class.simpleName}: ${e.message}" },
//            )
//        }
//
//        fun seriesImportEntry(series: OfflineSeries, error: Throwable? = null): OfflineLogEntry {
//            return OfflineLogEntry(
//                title = "Series Import",
//                description = series.name,
//                errorMessage = error?.let { e -> "${e::class.simpleName}: ${e.message}" },
//            )
//        }
//
//        fun libraryImportEntry(library: KomgaLibrary, error: Throwable? = null): OfflineLogEntry {
//            return OfflineLogEntry(
//                title = "Library Import",
//                description = library.name,
//                errorMessage = error?.let { e -> "${e::class.simpleName}: ${e.message}" },
//            )
//        }
//
//        fun libraryImportEntry(library: OfflineLibrary, error: Throwable? = null): OfflineLogEntry {
//            return OfflineLogEntry(
//                title = "Library Import",
//                description = library.name,
//                errorMessage = error?.let { e -> "${e::class.simpleName}: ${e.message}" },
//            )
//        }
//
//    }
}


@Serializable
sealed interface SyncData {

    @Serializable
    @SerialName("LocalLibraryInfo")
    data class LocalLibraryInfo(
        val libraryId: KomgaLibraryId,
        val title: String,
    ) : SyncData

    @Serializable
    @SerialName("LocalSeriesInfo")
    data class LocalSeriesInfo(
        val seriesId: KomgaSeriesId?,
        val title: String,
    ) : SyncData

    @Serializable
    @SerialName("LocalBookInfo")
    data class LocalBookInfo(
        val bookId: KomgaBookId,
        val title: String,
    ) : SyncData

    @Serializable
    @SerialName("RemoteBookReadProgress")
    data class RemoteBookReadProgress(
        val bookId: KomgaBookId,
        val title: String,
    ) : SyncData
}