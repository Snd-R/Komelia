package io.github.snd_r.komelia.ui.reader.image

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.image.UpsamplingMode
import io.github.snd_r.komelia.image.availableReduceKernels
import io.github.snd_r.komelia.image.availableUpsamplingModes
import io.github.snd_r.komelia.platform.CommonParcelable
import io.github.snd_r.komelia.platform.CommonParcelize
import io.github.snd_r.komelia.platform.CommonParcelizeRawValue
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.ui.BookSiblingsContext
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.oneshot.OneshotScreen
import io.github.snd_r.komelia.ui.reader.image.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode.Companion.NotFound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import snd.komelia.image.ReduceKernel
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest
import snd.komga.client.common.KomgaReadingDirection
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.series.KomgaSeries
import snd.komga.client.series.KomgaSeriesClient

typealias SpreadIndex = Int

class ReaderState(
    private val bookClient: KomgaBookClient,
    private val seriesClient: KomgaSeriesClient,
    private val readListClient: KomgaReadListClient,
    private val navigator: Navigator,
    private val appNotifications: AppNotifications,
    private val readerSettingsRepository: ImageReaderSettingsRepository,
    private val currentBookId: MutableStateFlow<KomgaBookId?>,
    private val markReadProgress: Boolean,
    private val stateScope: CoroutineScope,
    private val bookSiblingsContext: BookSiblingsContext,
    val pageChangeFlow: SharedFlow<Unit>,
) {
    val state = MutableStateFlow<LoadState<Unit>>(LoadState.Uninitialized)
    val expandImageSettings = MutableStateFlow(false)

    val booksState = MutableStateFlow<BookState?>(null)
    val series = MutableStateFlow<KomgaSeries?>(null)


    val readerType = MutableStateFlow(ReaderType.PAGED)
    val imageStretchToFit = MutableStateFlow(true)
    val cropBorders = MutableStateFlow(false)
    val readProgressPage = MutableStateFlow(1)

    val upsamplingMode = MutableStateFlow<UpsamplingMode>(UpsamplingMode.NEAREST)
    val downsamplingKernel = MutableStateFlow<ReduceKernel>(ReduceKernel.NEAREST)
    val linearLightDownsampling = MutableStateFlow(false)
    val availableUpsamplingModes = availableUpsamplingModes()
    val availableDownsamplingKernels = availableReduceKernels()

    val flashOnPageChange = MutableStateFlow(false)
    val flashDuration = MutableStateFlow(100L)
    val flashEveryNPages = MutableStateFlow(1)
    val flashWith = MutableStateFlow(ReaderFlashColor.BLACK)

    suspend fun initialize(bookId: KomgaBookId) {
        upsamplingMode.value = readerSettingsRepository.getUpsamplingMode().first()
        downsamplingKernel.value = readerSettingsRepository.getDownsamplingKernel().first()
        linearLightDownsampling.value = readerSettingsRepository.getLinearLightDownsampling().first()

        imageStretchToFit.value = readerSettingsRepository.getStretchToFit().first()
        cropBorders.value = readerSettingsRepository.getCropBorders().first()
        flashOnPageChange.value = readerSettingsRepository.getFlashOnPageChange().first()
        flashDuration.value = readerSettingsRepository.getFlashDuration().first()
        flashEveryNPages.value = readerSettingsRepository.getFlashEveryNPages().first()
        flashWith.value = readerSettingsRepository.getFlashWith().first()

        appNotifications.runCatchingToNotifications {
            state.value = LoadState.Loading
            val currentBooksState = booksState.value
            if (currentBooksState == null) state.value = LoadState.Loading
            val newBook = bookClient.getBook(bookId)

            val bookPages = loadBookPages(newBook.id)

            val prevBook = getPreviousBook(bookId)
            val prevBookPages = if (prevBook != null) loadBookPages(prevBook.id) else emptyList()
            val nextBook = getNextBook(bookId)
            val nextBookPages = if (nextBook != null) loadBookPages(nextBook.id) else emptyList()

            booksState.value = BookState(
                currentBook = newBook,
                currentBookPages = bookPages,
                previousBook = prevBook,
                previousBookPages = prevBookPages,
                nextBook = nextBook,
                nextBookPages = nextBookPages
            )

            val bookProgress = newBook.readProgress
            readProgressPage.value = when {
                bookProgress == null || bookProgress.completed -> 1
                else -> bookProgress.page
            }
            currentBookId.value = bookId

            val currentSeries = seriesClient.getOneSeries(newBook.seriesId)
            series.value = currentSeries
            readerType.value = when (currentSeries.metadata.readingDirection) {
                KomgaReadingDirection.LEFT_TO_RIGHT -> ReaderType.PAGED
                KomgaReadingDirection.RIGHT_TO_LEFT -> ReaderType.PAGED
                KomgaReadingDirection.WEBTOON -> CONTINUOUS
                KomgaReadingDirection.VERTICAL, null -> readerSettingsRepository.getReaderType().first()
            }

            state.value = LoadState.Success(Unit)
        }.onFailure { state.value = LoadState.Error(it) }
    }

    private suspend fun loadBookPages(bookId: KomgaBookId): List<PageMetadata> {
        val pages = bookClient.getBookPages(bookId)

        return pages.map {
            val width = it.width
            val height = it.height
            PageMetadata(
                bookId = bookId,
                pageNumber = it.number,
                size = if (width != null && height != null) IntSize(width, height) else null
            )
        }
    }

    private suspend fun getNextBook(currentBookId: KomgaBookId): KomgaBook? {
        return try {
            when (bookSiblingsContext) {
                is BookSiblingsContext.ReadList ->
                    readListClient.getBookSiblingNext(bookSiblingsContext.id, currentBookId)

                BookSiblingsContext.Series -> bookClient.getBookSiblingNext(currentBookId)
            }
        } catch (e: ClientRequestException) {
            if (e.response.status != NotFound) throw e
            else null
        }

    }

    private suspend fun getPreviousBook(currentBookId: KomgaBookId): KomgaBook? {
        return try {
            when (bookSiblingsContext) {
                is BookSiblingsContext.ReadList ->
                    readListClient.getBookSiblingPrevious(bookSiblingsContext.id, currentBookId)

                BookSiblingsContext.Series -> bookClient.getBookSiblingPrevious(currentBookId)
            }
        } catch (e: ClientRequestException) {
            if (e.response.status != NotFound) throw e
            else null
        }

    }

    suspend fun loadNextBook() {
        val booksState = requireNotNull(booksState.value)
        if (booksState.nextBook != null) {
            val nextBook = getNextBook(booksState.nextBook.id)
            val nextBookPages = if (nextBook != null) loadBookPages(nextBook.id) else emptyList()

            readProgressPage.value = 1
            this.booksState.value = BookState(
                currentBook = booksState.nextBook,
                currentBookPages = booksState.nextBookPages,
                previousBook = booksState.currentBook,
                previousBookPages = booksState.currentBookPages,

                nextBook = nextBook,
                nextBookPages = nextBookPages
            )
        } else {
            navigator replace MainScreen(
                if (booksState.currentBook.oneshot) OneshotScreen(booksState.currentBook, bookSiblingsContext)
                else SeriesScreen(booksState.currentBook.seriesId)
            )
        }
    }

    suspend fun loadPreviousBook() {
        val booksState = requireNotNull(booksState.value)
        if (booksState.previousBook != null) {
            val previousBook = getPreviousBook(booksState.previousBook.id)
            val previousBookPages =
                if (previousBook != null) loadBookPages(previousBook.id) else emptyList()

            readProgressPage.value = booksState.previousBookPages.size
            this.booksState.value = BookState(
                currentBook = booksState.previousBook,
                currentBookPages = booksState.previousBookPages,
                nextBook = booksState.currentBook,
                nextBookPages = booksState.currentBookPages,

                previousBook = previousBook,
                previousBookPages = previousBookPages,
            )
        } else
            appNotifications.add(AppNotification.Normal("You're at the beginning of the book"))
        return
    }

    fun onProgressChange(page: Int) {
        if (markReadProgress) {
            val currentBook = requireNotNull(booksState.value?.currentBook)
            stateScope.launch {
                bookClient.markReadProgress(
                    currentBook.id,
                    KomgaBookReadProgressUpdateRequest(page)
                )
            }
        }

        readProgressPage.value = page
    }

    fun onReaderTypeChange(type: ReaderType) {
        this.readerType.value = type
        stateScope.launch { readerSettingsRepository.putReaderType(type) }
    }

    fun onStretchToFitChange(stretch: Boolean) {
        imageStretchToFit.value = stretch
        stateScope.launch { readerSettingsRepository.putStretchToFit(stretch) }
    }

    fun onCropBordersChange(trim: Boolean) {
        cropBorders.value = trim
        stateScope.launch { readerSettingsRepository.putCropBorders(trim) }
    }

    fun onFlashEnabledChange(enabled: Boolean) {
        flashOnPageChange.value = enabled
        stateScope.launch { readerSettingsRepository.putFlashOnPageChange(enabled) }
    }

    fun onFlashDurationChange(duration: Long) {
        flashDuration.value = duration
        stateScope.launch { readerSettingsRepository.putFlashDuration(duration) }
    }

    fun onFlashEveryNPagesChange(pages: Int) {
        flashEveryNPages.value = pages
        stateScope.launch { readerSettingsRepository.putFlashEveryNPages(pages) }
    }

    fun onFlashWithChange(flashWith: ReaderFlashColor) {
        this.flashWith.value = flashWith
        stateScope.launch { readerSettingsRepository.putFlashWith(flashWith) }
    }

    fun onUpsamplingModeChange(mode: UpsamplingMode) {
        upsamplingMode.value = mode
        stateScope.launch { readerSettingsRepository.putUpsamplingMode(mode) }
    }

    fun onDownsamplingKernelChange(kernel: ReduceKernel) {
        downsamplingKernel.value = kernel
        stateScope.launch { readerSettingsRepository.putDownsamplingKernel(kernel) }
    }

    fun onLinearLightDownsamplingChange(linear: Boolean) {
        linearLightDownsampling.value = linear
        stateScope.launch { readerSettingsRepository.putLinearLightDownsampling(linear) }
    }

    fun onDispose() {
        currentBookId.value = null
    }
}

@CommonParcelize
data class PageMetadata(
    val bookId: @CommonParcelizeRawValue KomgaBookId,
    val pageNumber: Int,
    val size: @CommonParcelizeRawValue IntSize?,
) : CommonParcelable {
    fun isLandscape(): Boolean {
        if (size == null) return false
        return size.width > size.height
    }

    fun toPageId() = PageId(bookId.value, pageNumber)
}

data class BookState(
    val currentBook: KomgaBook,
    val currentBookPages: List<PageMetadata>,
    val previousBook: KomgaBook?,
    val previousBookPages: List<PageMetadata>,
    val nextBook: KomgaBook?,
    val nextBookPages: List<PageMetadata>,
)

enum class ReaderType {
    PAGED,
    CONTINUOUS
}

enum class ReaderFlashColor {
    BLACK,
    WHITE,
    WHITE_AND_BLACK,
}
