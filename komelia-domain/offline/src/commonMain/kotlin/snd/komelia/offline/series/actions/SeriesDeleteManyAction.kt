package snd.komelia.offline.series.actions

import kotlinx.coroutines.flow.MutableSharedFlow
import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.actions.BookDeleteManyAction
import snd.komelia.offline.book.repository.OfflineBookMetadataAggregationRepository
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komelia.offline.series.model.OfflineSeries
import snd.komelia.offline.series.repository.OfflineSeriesMetadataRepository
import snd.komelia.offline.series.repository.OfflineSeriesRepository
import snd.komelia.offline.series.repository.OfflineThumbnailSeriesRepository
import snd.komga.client.sse.KomgaEvent

class SeriesDeleteManyAction(
    private val seriesRepository: OfflineSeriesRepository,
    private val seriesMetadataRepository: OfflineSeriesMetadataRepository,
    private val seriesThumbnailSeriesRepository: OfflineThumbnailSeriesRepository,
    private val bookRepository: OfflineBookRepository,
    private val bookMetadataAggregationRepository: OfflineBookMetadataAggregationRepository,
    private val readProgressRepository: OfflineReadProgressRepository,
    private val bookDeleteManyAction: BookDeleteManyAction,
    private val komgaEvents: MutableSharedFlow<KomgaEvent>,
    private val transactionTemplate: TransactionTemplate,
) : OfflineAction {

    suspend fun execute(series: List<OfflineSeries>) {
        transactionTemplate.execute {
            val seriesIds = series.map { it.id }
            val books = bookRepository.findAllBySeriesIds(seriesIds)
            bookDeleteManyAction.execute(books)

            readProgressRepository.deleteBySeriesIds(seriesIds)
            seriesThumbnailSeriesRepository.deleteBySeriesIds(seriesIds)
            seriesMetadataRepository.delete(seriesIds)
            bookMetadataAggregationRepository.delete(seriesIds)

            seriesRepository.delete(seriesIds)
        }

        series.forEach { komgaEvents.emit(KomgaEvent.SeriesDeleted(it.id, it.libraryId)) }
    }
}