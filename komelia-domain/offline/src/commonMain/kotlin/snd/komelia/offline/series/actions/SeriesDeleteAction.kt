package snd.komelia.offline.series.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.actions.BookDeleteManyAction
import snd.komelia.offline.book.repository.OfflineBookMetadataAggregationRepository
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.series.repository.OfflineSeriesMetadataRepository
import snd.komelia.offline.series.repository.OfflineSeriesRepository
import snd.komelia.offline.series.repository.OfflineThumbnailSeriesRepository
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.sse.KomgaEvent

class SeriesDeleteAction(
    private val seriesRepository: OfflineSeriesRepository,
    private val seriesMetadataRepository: OfflineSeriesMetadataRepository,
    private val seriesThumbnailSeriesRepository: OfflineThumbnailSeriesRepository,
    private val bookMetadataAggregationRepository: OfflineBookMetadataAggregationRepository,
    private val bookRepository: OfflineBookRepository,
    private val bookDeleteManyAction: BookDeleteManyAction,
    private val transactionTemplate: TransactionTemplate,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
    private val isOffline: StateFlow<Boolean>,
) : OfflineAction {

    suspend fun execute(seriesId: KomgaSeriesId) {
        val series = transactionTemplate.execute {
            val series = seriesRepository.get(seriesId)
            val books = bookRepository.findAll(series.id)
            bookDeleteManyAction.execute(books)

            seriesThumbnailSeriesRepository.deleteBySeriesId(series.id)
            seriesMetadataRepository.delete(series.id)
            bookMetadataAggregationRepository.delete(seriesId)

            seriesRepository.delete(series.id)

            series
        }

        if (isOffline.value) {
            komgaEvents.emit(KomgaEvent.SeriesDeleted(series.id, series.libraryId))
        } else {
            komgaEvents.emit(KomgaEvent.SeriesChanged(series.id, series.libraryId))
        }

    }
}