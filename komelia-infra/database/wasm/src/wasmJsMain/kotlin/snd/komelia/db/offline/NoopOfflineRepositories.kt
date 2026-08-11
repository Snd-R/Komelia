package snd.komelia.db.offline

import io.github.vinceglb.filekit.BrowserFile
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.WebFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.offline.api.repository.OfflineBookDtoRepository
import snd.komelia.offline.api.repository.OfflineReferentialRepository
import snd.komelia.offline.api.repository.OfflineSeriesDtoRepository
import snd.komelia.offline.book.model.OfflineBook
import snd.komelia.offline.book.model.OfflineBookMetadata
import snd.komelia.offline.book.model.OfflineThumbnailBook
import snd.komelia.offline.book.repository.OfflineBookMetadataAggregationRepository
import snd.komelia.offline.book.repository.OfflineBookMetadataRepository
import snd.komelia.offline.book.repository.OfflineBookRepository
import snd.komelia.offline.book.repository.OfflineThumbnailBookRepository
import snd.komelia.offline.library.model.OfflineLibrary
import snd.komelia.offline.library.repository.OfflineLibraryRepository
import snd.komelia.offline.media.model.OfflineMedia
import snd.komelia.offline.media.repository.OfflineMediaRepository
import snd.komelia.offline.readprogress.OfflineReadProgress
import snd.komelia.offline.readprogress.OfflineReadProgressRepository
import snd.komelia.offline.series.model.OfflineBookMetadataAggregation
import snd.komelia.offline.series.model.OfflineSeries
import snd.komelia.offline.series.model.OfflineSeriesMetadata
import snd.komelia.offline.series.model.OfflineThumbnailSeries
import snd.komelia.offline.series.repository.OfflineSeriesMetadataRepository
import snd.komelia.offline.series.repository.OfflineSeriesRepository
import snd.komelia.offline.series.repository.OfflineThumbnailSeriesRepository
import snd.komelia.offline.server.model.OfflineMediaServer
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komelia.offline.server.repository.OfflineMediaServerRepository
import snd.komelia.offline.settings.OfflineSettingsRepository
import snd.komelia.offline.sync.model.LogEntryId
import snd.komelia.offline.sync.model.OfflineLogEntry
import snd.komelia.offline.sync.repository.LogJournalRepository
import snd.komelia.offline.tasks.model.TaskEntry
import snd.komelia.offline.tasks.repository.OfflineTasksRepository
import snd.komelia.offline.user.model.OfflineUser
import snd.komelia.offline.user.repository.OfflineUserRepository
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookSearch
import snd.komga.client.collection.KomgaCollectionId
import snd.komga.client.common.KomgaAuthor
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaThumbnailId
import snd.komga.client.common.Page
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.readlist.KomgaReadListId
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesId
import snd.komga.client.series.KomgaSeriesSearch
import snd.komga.client.user.KomgaUserId
import kotlin.time.Instant

class NoopOfflineMediaServerRepository : OfflineMediaServerRepository {
    override suspend fun save(server: OfflineMediaServer) {
    }

    override suspend fun get(id: OfflineMediaServerId): OfflineMediaServer {
        TODO("Not yet implemented")
    }

    override suspend fun find(id: OfflineMediaServerId): OfflineMediaServer? {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(): List<OfflineMediaServer> {
        TODO("Not yet implemented")
    }

    override suspend fun findByUrl(url: String): OfflineMediaServer? {
        TODO("Not yet implemented")
    }

    override suspend fun findByUserId(userId: KomgaUserId): OfflineMediaServer? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: OfflineMediaServerId) {
    }
}

class NoopOfflineMediaRepository : OfflineMediaRepository {
    override suspend fun save(media: OfflineMedia) {
        TODO("Not yet implemented")
    }

    override suspend fun find(id: KomgaBookId): OfflineMedia? {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(ids: List<KomgaBookId>): List<OfflineMedia> {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: KomgaBookId): OfflineMedia {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: KomgaBookId) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(bookIds: List<KomgaBookId>) {
        TODO("Not yet implemented")
    }
}

class NoopOfflineBookRepository : OfflineBookRepository {
    override suspend fun save(book: OfflineBook) {
        TODO("Not yet implemented")
    }

    override suspend fun find(id: KomgaBookId): OfflineBook? {
        TODO("Not yet implemented")
    }

    override suspend fun exists(id: KomgaBookId): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun findIn(ids: Collection<KomgaBookId>): List<OfflineBook> {
        TODO("Not yet implemented")
    }

    override suspend fun findFirstIdInSeriesOrNull(seriesId: KomgaSeriesId): KomgaBookId? {
        TODO("Not yet implemented")
    }

    override suspend fun findLastIdInSeriesOrNull(seriesId: KomgaSeriesId): KomgaBookId? {
        TODO("Not yet implemented")
    }

    override suspend fun findFirstUnreadIdInSeriesOrNull(
        seriesId: KomgaSeriesId,
        userId: KomgaUserId
    ): KomgaBookId? {
        TODO("Not yet implemented")
    }

    override suspend fun findAllBySeriesIds(seriesIds: List<KomgaSeriesId>): List<OfflineBook> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllIdsBySeriesId(seriesId: KomgaSeriesId): List<KomgaBookId> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllIdsByLibraryId(libraryId: KomgaLibraryId): List<KomgaBookId> {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: KomgaBookId): OfflineBook {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(id: KomgaSeriesId): List<OfflineBook> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllNotDeleted(id: KomgaSeriesId): List<OfflineBook> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: KomgaBookId) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(ids: Collection<KomgaBookId>) {
        TODO("Not yet implemented")
    }

}

class NoopOfflineBookMetadataRepository : OfflineBookMetadataRepository {
    override suspend fun save(metadata: OfflineBookMetadata) {
        TODO("Not yet implemented")
    }

    override suspend fun find(id: KomgaBookId): OfflineBookMetadata? {
        TODO("Not yet implemented")
    }

    override suspend fun findAllByIds(bookIds: List<KomgaBookId>): List<OfflineBookMetadata> {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: KomgaBookId): OfflineBookMetadata {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: KomgaBookId) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(bookIds: List<KomgaBookId>) {
        TODO("Not yet implemented")
    }

}

class NoopOfflineBookMetadataAggregationRepository : OfflineBookMetadataAggregationRepository {
    override suspend fun save(metadata: OfflineBookMetadataAggregation) {
        TODO("Not yet implemented")
    }

    override suspend fun find(seriesId: KomgaSeriesId): OfflineBookMetadataAggregation? {
        TODO("Not yet implemented")
    }

    override suspend fun get(seriesId: KomgaSeriesId): OfflineBookMetadataAggregation {
        TODO("Not yet implemented")
    }

    override suspend fun delete(seriesId: KomgaSeriesId) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(seriesIds: List<KomgaSeriesId>) {
        TODO("Not yet implemented")
    }
}

class NoopOfflineLibraryRepository : OfflineLibraryRepository {
    override suspend fun save(library: OfflineLibrary) {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: KomgaLibraryId): OfflineLibrary {
        TODO("Not yet implemented")
    }

    override suspend fun find(id: KomgaLibraryId): OfflineLibrary? {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(): List<OfflineLibrary> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllByMediaServer(mediaServerId: OfflineMediaServerId): List<OfflineLibrary> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: KomgaLibraryId) {
        TODO("Not yet implemented")
    }

}

class NoopOfflineReadProgressRepository : OfflineReadProgressRepository {
    override suspend fun save(readProgress: OfflineReadProgress) {
        TODO("Not yet implemented")
    }

    override suspend fun saveAll(readProgress: List<OfflineReadProgress>) {
        TODO("Not yet implemented")
    }

    override suspend fun find(
        bookId: KomgaBookId,
        userId: KomgaUserId
    ): OfflineReadProgress? {
        TODO("Not yet implemented")
    }

    override suspend fun findAllByBookIdsAndUserId(
        bookIds: List<KomgaBookId>,
        userId: KomgaUserId
    ): List<OfflineReadProgress> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllModifiedAfter(
        timestamp: Instant,
        userId: KomgaUserId,
        serverId: OfflineMediaServerId
    ): List<OfflineReadProgress> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllByServer(
        userId: KomgaUserId,
        serverId: OfflineMediaServerId
    ): List<OfflineReadProgress> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByUserId(userId: KomgaUserId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByBookIdsAndUserId(
        bookIds: List<KomgaBookId>,
        userId: KomgaUserId
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBySeriesIds(seriesIds: List<KomgaSeriesId>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByBookIds(bookIds: List<KomgaBookId>) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(bookId: KomgaBookId, userId: KomgaUserId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllBy(bookId: KomgaBookId) {
        TODO("Not yet implemented")
    }
}

class NoopOfflineSeriesMetadataRepository : OfflineSeriesMetadataRepository {
    override suspend fun save(metadata: OfflineSeriesMetadata) {
        TODO("Not yet implemented")
    }

    override suspend fun find(id: KomgaSeriesId): OfflineSeriesMetadata? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: KomgaSeriesId) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(seriesIds: List<KomgaSeriesId>) {
        TODO("Not yet implemented")
    }
}

class NoopOfflineSeriesRepository : OfflineSeriesRepository {
    override suspend fun save(series: OfflineSeries) {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: KomgaSeriesId): OfflineSeries {
        TODO("Not yet implemented")
    }

    override suspend fun find(id: KomgaSeriesId): OfflineSeries? {
        TODO("Not yet implemented")
    }

    override suspend fun findAllByLibraryId(libraryId: KomgaLibraryId): List<OfflineSeries> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: KomgaSeriesId) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(seriesids: List<KomgaSeriesId>) {
        TODO("Not yet implemented")
    }

}

class NoopOfflineThumbnailBookRepository : OfflineThumbnailBookRepository {
    override suspend fun save(thumbnail: OfflineThumbnailBook) {
        TODO("Not yet implemented")
    }

    override suspend fun find(id: KomgaThumbnailId): OfflineThumbnailBook? {
        TODO("Not yet implemented")
    }

    override suspend fun findSelectedByBookId(bookId: KomgaBookId): OfflineThumbnailBook? {
        TODO("Not yet implemented")
    }

    override suspend fun findAllByBookId(bookId: KomgaBookId): List<OfflineThumbnailBook> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllByBookIdAndType(
        bookId: KomgaBookId,
        type: Collection<OfflineThumbnailBook.Type>
    ): List<OfflineThumbnailBook> {
        TODO("Not yet implemented")
    }

    override suspend fun markSelected(thumbnail: OfflineThumbnailBook) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: KomgaThumbnailId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByBookIdAndType(
        id: KomgaBookId,
        type: OfflineThumbnailBook.Type
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllBy(id: KomgaBookId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteByBookIds(bookIds: Collection<KomgaBookId>) {
        TODO("Not yet implemented")
    }
}

class NoopOfflineThumbnailSeriesRepository : OfflineThumbnailSeriesRepository {
    override suspend fun save(thumbnail: OfflineThumbnailSeries) {
        TODO("Not yet implemented")
    }

    override suspend fun find(thumbnailId: KomgaThumbnailId): OfflineThumbnailSeries? {
        TODO("Not yet implemented")
    }

    override suspend fun findSelectedBySeriesId(seriesId: KomgaSeriesId): OfflineThumbnailSeries? {
        TODO("Not yet implemented")
    }

    override suspend fun findAllBySeriesId(seriesId: KomgaSeriesId): Collection<OfflineThumbnailSeries> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllBySeriesIdAndType(
        seriesId: KomgaSeriesId,
        type: OfflineThumbnailSeries.Type
    ): List<OfflineThumbnailSeries> {
        TODO("Not yet implemented")
    }

    override suspend fun markSelected(thumbnail: OfflineThumbnailSeries) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(thumbnailSeriesId: KomgaThumbnailId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBySeriesId(seriesId: KomgaSeriesId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBySeriesIds(seriesIds: List<KomgaSeriesId>) {
        TODO("Not yet implemented")
    }
}

class NoopOfflineUserRepository : OfflineUserRepository {
    override suspend fun save(user: OfflineUser) {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: KomgaUserId): OfflineUser {
        TODO("Not yet implemented")
    }

    override suspend fun find(id: KomgaUserId): OfflineUser? {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(): List<OfflineUser> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllByServer(serverId: OfflineMediaServerId): List<OfflineUser> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: KomgaUserId) {
        TODO("Not yet implemented")
    }
}

class NoopOfflineBookDtoRepository : OfflineBookDtoRepository {
    override suspend fun findAll(
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest
    ): Page<KomeliaBook> {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(
        userId: KomgaUserId,
        search: KomgaBookSearch,
        pageRequest: KomgaPageRequest
    ): Page<KomeliaBook> {
        TODO("Not yet implemented")
    }

    override suspend fun get(
        bookId: KomgaBookId,
        userId: KomgaUserId
    ): KomeliaBook {
        TODO("Not yet implemented")
    }

    override suspend fun findByIdOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId
    ): KomeliaBook? {
        TODO("Not yet implemented")
    }

    override suspend fun findPreviousInSeriesOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId
    ): KomeliaBook? {
        TODO("Not yet implemented")
    }

    override suspend fun findNextInSeriesOrNull(
        bookId: KomgaBookId,
        userId: KomgaUserId
    ): KomeliaBook? {
        TODO("Not yet implemented")
    }

    override suspend fun findAllOnDeck(
        userId: KomgaUserId,
        filterOnLibraryIds: Collection<KomgaLibraryId>?,
        pageRequest: KomgaPageRequest
    ): Page<KomeliaBook> {
        TODO("Not yet implemented")
    }

}

class NoopOfflineReferentialRepository : OfflineReferentialRepository {
    override suspend fun findAllAuthorsByName(search: String): List<KomgaAuthor> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAuthorsByNameAndLibrary(
        search: String,
        libraryId: KomgaLibraryId
    ): List<KomgaAuthor> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAuthorsByNameAndCollection(
        search: String,
        collectionId: KomgaCollectionId
    ): List<KomgaAuthor> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAuthorsByNameAndSeries(
        search: String,
        seriesId: KomgaSeriesId
    ): List<KomgaAuthor> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAuthorsNamesByName(search: String): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAuthorsRoles(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAuthorsByName(
        search: String?,
        role: String?,
        pageRequest: KomgaPageRequest
    ): Page<KomgaAuthor> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAuthorsByNameAndLibraries(
        search: String?,
        role: String?,
        libraryIds: List<KomgaLibraryId>,
        pageRequest: KomgaPageRequest
    ): Page<KomgaAuthor> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAuthorsByNameAndCollection(
        search: String?,
        role: String?,
        collectionId: KomgaCollectionId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaAuthor> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAuthorsByNameAndSeries(
        search: String?,
        role: String?,
        seriesId: KomgaSeriesId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaAuthor> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAuthorsByNameAndReadList(
        search: String?,
        role: String?,
        readListId: KomgaReadListId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaAuthor> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllGenres(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllGenresByLibraries(libraryIds: List<KomgaLibraryId>): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllGenresByCollection(collectionId: KomgaCollectionId): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSeriesAndBookTags(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSeriesAndBookTagsByLibraries(libraryIds: List<KomgaLibraryId>): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSeriesAndBookTagsByCollection(collectionId: KomgaCollectionId): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSeriesTags(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSeriesTagsByLibrary(libraryId: KomgaLibraryId): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSeriesTagsByCollection(collectionId: KomgaCollectionId): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllBookTags(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllBookTagsBySeries(seriesId: KomgaSeriesId): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllBookTagsByReadList(readListId: KomgaReadListId): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllLanguages(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllLanguagesByLibraries(libraryIds: List<KomgaLibraryId>): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllLanguagesByCollection(collectionId: KomgaCollectionId): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllPublishers(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllPublishers(pageable: KomgaPageRequest): Page<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllPublishersByLibraries(libraryIds: List<KomgaLibraryId>): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllPublishersByCollection(collectionId: KomgaCollectionId): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAgeRatings(): List<Int?> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAgeRatingsByLibraries(libraryIds: List<KomgaLibraryId>): List<Int?> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllAgeRatingsByCollection(collectionId: KomgaCollectionId): List<Int?> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSeriesReleaseDates(): List<LocalDate> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSeriesReleaseDatesByLibraries(libraryIds: List<KomgaLibraryId>): List<LocalDate> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSeriesReleaseDatesByCollection(collectionId: KomgaCollectionId): List<LocalDate> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSharingLabels(): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSharingLabelsByLibraries(libraryIds: List<KomgaLibraryId>): List<String> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllSharingLabelsByCollection(collectionId: KomgaCollectionId): List<String> {
        TODO("Not yet implemented")
    }
}

class NoopOfflineSeriesDtoRepository : OfflineSeriesDtoRepository {
    override suspend fun get(
        seriesId: KomgaSeriesId,
        userId: KomgaUserId
    ): KomgaSeries {
        TODO("Not yet implemented")
    }

    override suspend fun find(
        seriesId: KomgaSeriesId,
        userId: KomgaUserId
    ): KomgaSeries? {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaSeries> {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(
        search: KomgaSeriesSearch,
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaSeries> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllRecentlyUpdated(
        search: KomgaSeriesSearch,
        userId: KomgaUserId,
        pageRequest: KomgaPageRequest
    ): Page<KomgaSeries> {
        TODO("Not yet implemented")
    }
}

class NoopLogJournalRepository : LogJournalRepository {
    override suspend fun save(entry: OfflineLogEntry) {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: LogEntryId): OfflineLogEntry {
        TODO("Not yet implemented")
    }

    override suspend fun findAll(
        limit: Int,
        offset: Long
    ): Page<OfflineLogEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun findAllByType(
        type: OfflineLogEntry.Type,
        limit: Int,
        offset: Long
    ): Page<OfflineLogEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }
}

class NoopOfflineTasksRepository : OfflineTasksRepository {
    override suspend fun takeNew(): TaskEntry? {
        TODO("Not yet implemented")
    }

    override suspend fun save(entry: TaskEntry) {
        TODO("Not yet implemented")
    }

    override suspend fun save(tasks: Collection<TaskEntry>) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(taskId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun resetAllRunning(): Int {
        TODO("Not yet implemented")
    }
}

class NoopOfflineSettingsRepository : OfflineSettingsRepository {
    override fun getOfflineMode(): Flow<Boolean> {
        return flowOf(false)
    }

    override suspend fun putOfflineMode(offline: Boolean) {
    }

    override fun getUserId(): Flow<KomgaUserId> {
        return flowOf(KomgaUserId("123"))
    }

    override suspend fun putUserId(userId: KomgaUserId) {
    }

    override fun getReadProgressSyncDate(): Flow<Instant?> {
        return flowOf(null)
    }

    override suspend fun putReadProgressSyncDate(timestamp: Instant) {
    }

    override fun getDataSyncDate(): Flow<Instant?> {
        return flowOf(null)
    }

    override suspend fun putDataSyncDate(timestamp: Instant) {
    }

    override fun getDownloadDirectory(): Flow<PlatformFile> {
        return flowOf(
            PlatformFile(
                WebFile.FileWrapper(
                    file = BrowserFile(
                        fileBits = emptyList<JsAny?>().toJsArray(),
                        fileName = "noop",
                    ),
                )
            )
        )
    }

    override suspend fun putDownloadDirectory(path: PlatformFile) {
    }
}
