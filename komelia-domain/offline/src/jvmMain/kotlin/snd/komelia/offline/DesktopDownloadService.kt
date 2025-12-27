package snd.komelia.offline

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

//class DesktopDownloadService(
//    private val libraryDownloadPath: Flow<kotlinx.io.files.Path>,
//    downloadEvents: MutableSharedFlow<DownloadEvent>,
//    bookClient: KomgaBookClient,
//) : BookDownloadService(downloadEvents, bookClient) {
//
//
////    override suspend fun downloadBook(bookId: KomgaBookId) {
////        val book = bookClient.getOne(bookId)
////        downloadBook(book)
////    }
//
//    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
//
////    override suspend fun downloadSeries(series: KomgaSeries) {
////        try {
////            val books = bookClient.getBookList(
////                anyOfBooks { seriesId { isEqualTo(series.id) } },
////                pageRequest = KomgaPageRequest(unpaged = true)
////            )
////
////            books.content.forEach {
////                coroutineScope.launch { downloadBook(it) }
////            }
////        } catch (e: Exception) {
////            currentCoroutineContext().ensureActive()
////            logger.catching(e)
////        }
////    }
//
////    override suspend fun downloadBook(book: KomgaBook) {
////        try {
////            val libraryDirectory = Path.of(libraryDownloadPath.first().toString())
////            val seriesDirectory = libraryDirectory.resolve(book.seriesId.value)
////            val bookFile = seriesDirectory.resolve(book.name)
////            bookFile.createParentDirectories()
////            bookFile.deleteIfExists()
////
////            bookClient.getBookFile(book.id) { response ->
////                downloadToFile(bookFile, response)
////                    .catch { error ->
////                        logger.catching(error)
////                        downloadEvents.emit(DownloadEvent.BookDownloadError(book, error))
////                    }
////                    .conflate()
////                    .collect { newProgress ->
////                        downloadEvents.emit(
////                            DownloadEvent.BookDownloadProgress(
////                                book = book,
////                                total = newProgress.total,
////                                completed = newProgress.completed,
////                                downloadPath = kotlinx.io.files.Path(bookFile.toString())
////                            )
////                        )
////                    }
////            }
////
////            downloadEvents.emit(
////                DownloadEvent.BookDownloadCompleted(
////                    book = book,
////                    downloadPath = kotlinx.io.files.Path(bookFile.toString())
////                )
////            )
////        } catch (e: Exception) {
////            currentCoroutineContext().ensureActive()
////            downloadEvents.emit(DownloadEvent.BookDownloadError(book, e))
////        }
////    }
//
//    private fun downloadToFile(file: Path, response: HttpResponse): Flow<DownloadProgress> {
//        return flow {
//            val tempFile = createTempFile(file.parent)
//            tempFile.toFile().deleteOnExit()
//            val length = response.headers["Content-Length"]?.toLong() ?: 0L
//            val url = response.request.url.toString()
//            emit(DownloadProgress(length, 0, url))
//            val channel = response.bodyAsChannel().counted()
//
//            tempFile.outputStream().buffered().use { outStream ->
//                while (!channel.isClosedForRead) {
//                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
//                    while (!packet.exhausted()) {
//                        val bytes = packet.readByteArray()
//                        outStream.write(bytes)
//                    }
//                    emit(DownloadProgress(length, channel.totalBytesRead, url))
//                }
//            }
//            tempFile.moveTo(file, true)
//        }
//    }
//
//    private data class DownloadProgress(
//        val total: Long,
//        val completed: Long,
//        val url: String,
//    )
//}