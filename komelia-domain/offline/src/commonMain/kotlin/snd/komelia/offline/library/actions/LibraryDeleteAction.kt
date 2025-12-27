package snd.komelia.offline.library.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.library.repository.OfflineLibraryRepository
import snd.komelia.offline.series.actions.SeriesDeleteManyAction
import snd.komelia.offline.series.repository.OfflineSeriesRepository
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.sse.KomgaEvent

class LibraryDeleteAction(
    private val libraryRepository: OfflineLibraryRepository,
    private val seriesRepository: OfflineSeriesRepository,
    private val seriesDeleteManyAction: SeriesDeleteManyAction,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>
) : OfflineAction {

    suspend fun execute(libraryId: KomgaLibraryId) {
        transactionTemplate.execute {
            val series = seriesRepository.findAllByLibraryId(libraryId)
            seriesDeleteManyAction.execute(series)
            libraryRepository.delete(libraryId)
        }

        komgaEvents.emit(KomgaEvent.LibraryDeleted(libraryId))
    }
}