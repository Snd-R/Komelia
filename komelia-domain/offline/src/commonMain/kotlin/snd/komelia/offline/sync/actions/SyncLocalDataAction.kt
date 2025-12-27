package snd.komelia.offline.sync.actions

//private val logger = KotlinLogging.logger { }
//
//class SyncLocalDataAction(
//    private val bookClient: KomgaBookClient,
//    private val seriesClient: KomgaSeriesClient,
//    private val libraryClient: KomgaLibraryClient,
//
//    private val libraryRepository: OfflineLibraryRepository,
//    private val seriesRepository: OfflineSeriesRepository,
//    private val bookRepository: OfflineBookRepository,
//    private val mediaServerRepository: OfflineMediaServerRepository,
//    private val syncJournalRepository: SyncJournalRepository,
//    private val transactionTemplate: TransactionTemplate,
//
//    private val userSaveAction: UserKomgaImportAction,
//    private val libraryImportAction: LibraryKomgaImportAction,
//    private val seriesImportAction: SeriesKomgaImportAction,
//    private val bookImportAction: BookKomgaImportAction,
//    private val bookMarkDeletedAction: BookMarkRemoteDeletedAction,
//) : Action {
//
//    suspend fun execute(onlineUser: KomgaUser) {
//        transactionTemplate.execute {
//            val server = mediaServerRepository.findByUserId(onlineUser.id) ?: return
//
//            if (onlineUser.id != OfflineUser.ROOT) {
//                userSaveAction.execute(onlineUser, server.id)
//            }
//
//            syncLibraryData(onlineUser, server)
//        }
//
//    }
//
//    private suspend fun syncLibraryData(onlineUser: KomgaUser, server: OfflineMediaServer) {
//        val localLibraries = libraryRepository.findAllByMediaServer(server.id)
//        for (library in localLibraries) {
//            try {
//                val remoteLibrary = libraryClient.getLibrary(library.id)
//                libraryImportAction.execute(remoteLibrary, server.id)
//                syncSeriesData(remoteLibrary, onlineUser.id)
//            } catch (e: ClientRequestException) {
//                logger.catching(e)
//            }
//        }
//
//    }
//
//    private suspend fun syncSeriesData(library: KomgaLibrary, userId: KomgaUserId) {
//        val series = seriesRepository.findAllByLibraryId(library.id)
//        for (offlineSeries in series) {
//            try {
//                val remoteSeries = seriesClient.getOneSeries(offlineSeries.id)
//                seriesImportAction.execute(remoteSeries)
//                syncBookData(remoteSeries, userId)
//            } catch (e: ClientRequestException) {
//                logger.catching(e)
//                syncJournalRepository.save(seriesInfoEntry(offlineSeries, e))
//            }
//        }
//    }
//
//    private suspend fun syncBookData(series: KomgaSeries, userId: KomgaUserId) {
//        val books = bookRepository.findAllNotDeleted(series.id)
//        for (localBook in books) {
//            try {
//                val remoteBook = bookClient.getOne(localBook.id)
//                bookImportAction.execute(
//                    book = remoteBook,
//                    offlinePath = localBook.fileDownloadPath,
//                    userId = userId,
//                    localFileModifiedDate = localBook.localFileLastModified
//                )
//            } catch (e: ClientRequestException) {
//                logger.error(e) { "marking book ${localBook.id} as unavailable" }
//                syncJournalRepository.save(bookInfoEntry(localBook, e))
//                if (e.response.status == HttpStatusCode.NotFound) {
//                    bookMarkDeletedAction.execute(localBook.id)
//                }
//            }
//        }
//    }
//}