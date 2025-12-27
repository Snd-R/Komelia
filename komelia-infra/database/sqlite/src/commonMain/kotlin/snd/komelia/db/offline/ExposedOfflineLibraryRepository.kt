package snd.komelia.db.offline

import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.offline.tables.OfflineLibraryExclusionsTable
import snd.komelia.db.offline.tables.OfflineLibraryTable
import snd.komelia.offline.library.model.OfflineLibrary
import snd.komelia.offline.library.repository.OfflineLibraryRepository
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komga.client.library.KomgaLibraryId
import snd.komga.client.library.ScanInterval
import snd.komga.client.library.SeriesCover

class ExposedOfflineLibraryRepository( database: Database) : ExposedRepository(database), OfflineLibraryRepository {
    private val libraryTable = OfflineLibraryTable
    private val libraryExclusionsTable = OfflineLibraryExclusionsTable

    override suspend fun save(library: OfflineLibrary) {
        transaction {
            libraryTable.upsert {
                it[libraryTable.id] = library.id.value
                it[libraryTable.mediaServerId] = library.mediaServerId.value
                it[libraryTable.name] = library.name
                it[libraryTable.root] = library.root
                it[libraryTable.importComicInfoBook] = library.importComicInfoBook
                it[libraryTable.importComicInfoSeries] = library.importComicInfoSeries
                it[libraryTable.importComicInfoSeriesAppendVolume] = library.importComicInfoSeriesAppendVolume
                it[libraryTable.importComicInfoCollection] = library.importComicInfoCollection
                it[libraryTable.importComicInfoReadList] = library.importComicInfoReadList
                it[libraryTable.importEpubBook] = library.importEpubBook
                it[libraryTable.importEpubSeries] = library.importEpubSeries
                it[libraryTable.importMylarSeries] = library.importMylarSeries
                it[libraryTable.importLocalArtwork] = library.importLocalArtwork
                it[libraryTable.importBarcodeIsbn] = library.importBarcodeIsbn
                it[libraryTable.scanForceModifiedTime] = library.scanForceModifiedTime
                it[libraryTable.scanOnStartup] = library.scanOnStartup
                it[libraryTable.scanInterval] = library.scanInterval.name
                it[libraryTable.scanCbx] = library.scanCbx
                it[libraryTable.scanPdf] = library.scanPdf
                it[libraryTable.scanEpub] = library.scanEpub
                it[libraryTable.repairExtensions] = library.repairExtensions
                it[libraryTable.convertToCbz] = library.convertToCbz
                it[libraryTable.emptyTrashAfterScan] = library.emptyTrashAfterScan
                it[libraryTable.seriesCover] = library.seriesCover.name
                it[libraryTable.hashFiles] = library.hashFiles
                it[libraryTable.hashPages] = library.hashPages
                it[libraryTable.hashKoreader] = library.hashKoreader
                it[libraryTable.analyzeDimensions] = library.analyzeDimensions
                it[libraryTable.oneshotsDirectory] = library.oneshotsDirectory
                it[libraryTable.unavailable] = library.unavailable
            }
        }
    }

    override suspend fun get(id: KomgaLibraryId): OfflineLibrary {
        return find(id) ?: throw IllegalStateException("Library $id is not found")
    }

    override suspend fun find(id: KomgaLibraryId): OfflineLibrary? {
        return transaction {
            selectBase()
                .where(libraryTable.id.eq(id.value))
                .fetchAndMap()
                .firstOrNull()
        }
    }

    override suspend fun findAll(): List<OfflineLibrary> {
        return transaction {
            selectBase().fetchAndMap()
        }
    }

    override suspend fun findAllByMediaServer(mediaServerId: OfflineMediaServerId): List<OfflineLibrary> {
        return transaction {
            selectBase()
                .where { libraryTable.mediaServerId.eq(mediaServerId.value) }
                .fetchAndMap()
        }
    }

    override suspend fun delete(id: KomgaLibraryId) {
        transaction {
            libraryExclusionsTable.deleteWhere { libraryExclusionsTable.libraryId.eq(id.value) }
            libraryTable.deleteWhere { libraryTable.id.eq(id.value) }
        }
    }

    private fun findOne(libraryId: KomgaLibraryId): Query {
        return selectBase()
            .where(libraryTable.id.eq(libraryId.value))
    }

    private fun selectBase(): Query {
        return libraryTable
            .join(
                otherTable = libraryExclusionsTable,
                joinType = JoinType.LEFT,
                onColumn = libraryTable.id,
                otherColumn = libraryExclusionsTable.libraryId,
            )
            .selectAll()
    }

    private fun Query.fetchAndMap(): List<OfflineLibrary> {
        return this.groupBy({ it[libraryTable.id] }, { it }).values
            .map { rows ->
                val exclusions = rows.map { it[libraryExclusionsTable.exclusion] }
                rows.first().toModel(exclusions)
            }
    }

    private fun ResultRow.toModel(exclusions: List<String>): OfflineLibrary {
        return OfflineLibrary(
            id = KomgaLibraryId(this[libraryTable.id]),
            mediaServerId = OfflineMediaServerId(this[libraryTable.mediaServerId]),
            name = this[libraryTable.name],
            root = this[libraryTable.root],
            importComicInfoBook = this[libraryTable.importComicInfoBook],
            importComicInfoSeries = this[libraryTable.importComicInfoSeries],
            importComicInfoCollection = this[libraryTable.importComicInfoCollection],
            importComicInfoReadList = this[libraryTable.importComicInfoReadList],
            importComicInfoSeriesAppendVolume = this[libraryTable.importComicInfoSeriesAppendVolume],
            importEpubBook = this[libraryTable.importEpubBook],
            importEpubSeries = this[libraryTable.importEpubSeries],
            importMylarSeries = this[libraryTable.importMylarSeries],
            importLocalArtwork = this[libraryTable.importLocalArtwork],
            importBarcodeIsbn = this[libraryTable.importBarcodeIsbn],
            scanForceModifiedTime = this[libraryTable.scanForceModifiedTime],
            scanInterval = ScanInterval.valueOf(this[libraryTable.scanInterval]),
            scanOnStartup = this[libraryTable.scanOnStartup],
            scanCbx = this[libraryTable.scanCbx],
            scanPdf = this[libraryTable.scanPdf],
            scanEpub = this[libraryTable.scanEpub],
            scanDirectoryExclusions = exclusions,
            repairExtensions = this[libraryTable.repairExtensions],
            convertToCbz = this[libraryTable.convertToCbz],
            emptyTrashAfterScan = this[libraryTable.emptyTrashAfterScan],
            seriesCover = SeriesCover.valueOf(this[libraryTable.seriesCover]),
            hashFiles = this[libraryTable.hashFiles],
            hashPages = this[libraryTable.hashPages],
            hashKoreader = this[libraryTable.hashKoreader],
            analyzeDimensions = this[libraryTable.analyzeDimensions],
            oneshotsDirectory = this[libraryTable.oneshotsDirectory],
            unavailable = this[libraryTable.unavailable]
        )
    }
}