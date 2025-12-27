package snd.komelia.offline.series.actions

import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.model.OfflineBookMetadata
import snd.komelia.offline.book.repository.OfflineBookMetadataAggregationRepository
import snd.komelia.offline.book.repository.OfflineBookMetadataRepository
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.series.model.OfflineBookMetadataAggregation
import snd.komga.client.series.KomgaSeriesId

class SeriesAggregateBookMetadataAction(
    private val bookRepository: OfflineBookRepository,
    private val bookMetadataRepository: OfflineBookMetadataRepository,
    private val bookMetadataAggregationRepository: OfflineBookMetadataAggregationRepository,
    private val transactionTemplate: TransactionTemplate
) : OfflineAction {

    suspend fun execute(seriesId: KomgaSeriesId) {
        transactionTemplate.execute {
            val bookIds = bookRepository.findAllIdsBySeriesId(seriesId)
            val bookMetadata = bookMetadataRepository.findAllByIds(bookIds)
            val aggregation = aggregate(seriesId, bookMetadata)
            bookMetadataAggregationRepository.save(aggregation)
        }
    }

    private fun aggregate(
        seriesId: KomgaSeriesId,
        metadata: List<OfflineBookMetadata>,
    ): OfflineBookMetadataAggregation {
        val authors = metadata.flatMap { it.authors }.distinctBy { "${it.role}__${it.name}" }
        val tags = metadata.flatMap { it.tags }
        val (summary, summaryNumber) =
            metadata
                .sortedBy { it.numberSort }
                .find { it.summary.isNotBlank() }
                ?.let {
                    it.summary to it.number
                } ?: ("" to "")
        val releaseDate = metadata.mapNotNull { it.releaseDate }.minOrNull()

        return OfflineBookMetadataAggregation(
            seriesId = seriesId,
            authors = authors,
            tags = tags.toSet(),
            releaseDate = releaseDate,
            summary = summary,
            summaryNumber = summaryNumber
        )
    }
}