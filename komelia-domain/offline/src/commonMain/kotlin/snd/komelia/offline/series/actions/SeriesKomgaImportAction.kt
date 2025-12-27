package snd.komelia.offline.series.actions

import snd.komelia.db.TransactionTemplate
import snd.komelia.offline.action.OfflineAction
import snd.komelia.offline.book.repository.OfflineBookMetadataAggregationRepository
import snd.komelia.offline.series.model.OfflineBookMetadataAggregation
import snd.komelia.offline.series.model.OfflineSeries
import snd.komelia.offline.series.model.OfflineSeriesMetadata
import snd.komelia.offline.series.model.toOfflineThumbnailSeries
import snd.komelia.offline.series.repository.OfflineSeriesMetadataRepository
import snd.komelia.offline.series.repository.OfflineSeriesRepository
import snd.komelia.offline.series.repository.OfflineThumbnailSeriesRepository
import snd.komelia.offline.sync.model.OfflineLogEntry.Companion.logError
import snd.komelia.offline.sync.model.OfflineLogEntry.Companion.logInfo
import snd.komelia.offline.sync.repository.LogJournalRepository
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesMetadata

class SeriesKomgaImportAction(
    private val seriesRepository: OfflineSeriesRepository,
    private val seriesMetadataRepository: OfflineSeriesMetadataRepository,
    private val thumbnailSeriesRepository: OfflineThumbnailSeriesRepository,
    private val bookMetadataAggregationRepository: OfflineBookMetadataAggregationRepository,
    private val logJournalRepository: LogJournalRepository,
    private val seriesClient: KomgaSeriesClient,
    private val transactionTemplate: TransactionTemplate,
) : OfflineAction {

    suspend fun execute(series: KomgaSeries) {
        try {
            transactionTemplate.execute {
                doImport(series)
                logJournalRepository.logInfo { "Series updated '${series.metadata.title}'" }
            }
        } catch (e: Exception) {
            logJournalRepository.logError(e) { "Series update error '${series.metadata.title}'" }
            throw e
        }

    }

    private suspend fun doImport(series: KomgaSeries) {
        val offlineSeries = series.toOfflineSeries()
        val offlineSeriesMetadata = series.metadata.toOfflineMetadata(series.id)

        val offlineSeriesThumbnail = seriesClient.getThumbnails(series.id)
            .firstOrNull { it.selected }
            ?.let { thumb ->
                val thumbnailBytes = seriesClient.getThumbnail(thumb.seriesId, thumb.id)
                thumb.toOfflineThumbnailSeries(thumbnailBytes)
            }
        seriesRepository.save(offlineSeries)
        seriesMetadataRepository.save(offlineSeriesMetadata)
        bookMetadataAggregationRepository.save(OfflineBookMetadataAggregation(seriesId = offlineSeries.id))
        offlineSeriesThumbnail?.let { thumbnailSeriesRepository.save(it) }
    }

    private fun KomgaSeries.toOfflineSeries() =
        OfflineSeries(
            id = this.id,
            libraryId = this.libraryId,
            name = this.name,
            url = this.url,
            oneshot = this.oneshot,

            bookCount = this.booksCount,
            deleted = this.deleted,
            created = this.created,
            lastModified = this.lastModified,
            fileLastModified = this.fileLastModified,
        )

    fun KomgaSeriesMetadata.toOfflineMetadata(seriesId: KomgaSeriesId) =
        OfflineSeriesMetadata(
            seriesId = seriesId,
            status = this.status,
            statusLock = this.statusLock,
            title = this.title,
            alternateTitles = this.alternateTitles,
            alternateTitlesLock = this.alternateTitlesLock,
            titleLock = this.titleLock,
            titleSort = this.titleSort,
            titleSortLock = this.titleSortLock,
            summary = this.summary,
            summaryLock = this.summaryLock,
            readingDirection = this.readingDirection,
            readingDirectionLock = this.readingDirectionLock,
            publisher = this.publisher,
            publisherLock = this.publisherLock,
            ageRating = this.ageRating,
            ageRatingLock = this.ageRatingLock,
            language = this.language,
            languageLock = this.languageLock,
            genres = this.genres,
            genresLock = this.genresLock,
            tags = this.tags,
            tagsLock = this.tagsLock,
            totalBookCount = this.totalBookCount,
            totalBookCountLock = this.totalBookCountLock,
            sharingLabels = this.sharingLabels,
            sharingLabelsLock = this.sharingLabelsLock,
            links = this.links,
            linksLock = this.linksLock
        )
}