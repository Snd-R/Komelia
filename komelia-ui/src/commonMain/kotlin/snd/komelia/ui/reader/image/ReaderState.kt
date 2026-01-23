package snd.komelia.ui.reader.image

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.navigator.Navigator
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode.Companion.NotFound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
import snd.komelia.color.repository.BookColorCorrectionRepository
import snd.komelia.image.ReaderImage.PageId
import snd.komelia.image.ReduceKernel
import snd.komelia.image.UpsamplingMode
import snd.komelia.image.availableReduceKernels
import snd.komelia.image.availableUpsamplingModes
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.settings.model.ReaderFlashColor
import snd.komelia.settings.model.ReaderType
import snd.komelia.ui.BookSiblingsContext
import snd.komelia.ui.LoadState
import snd.komelia.ui.MainScreen
import snd.komelia.ui.oneshot.OneshotScreen
import snd.komelia.ui.platform.CommonParcelable
import snd.komelia.ui.platform.CommonParcelize
import snd.komelia.ui.platform.CommonParcelizeRawValue
import snd.komelia.ui.series.SeriesScreen
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest
import snd.komga.client.common.KomgaReadingDirection
import snd.komga.client.series.KomgaSeries

typealias SpreadIndex = Int

class ReaderState(
    private val bookApi: KomgaBookApi,
    private val seriesApi: KomgaSeriesApi,
    private val readListApi: KomgaReadListApi,
    private val navigator: Navigator,
    private val appNotifications: AppNotifications,
    private val readerSettingsRepository: ImageReaderSettingsRepository,
    private val currentBookId: MutableStateFlow<KomgaBookId?>,
    private val markReadProgress: Boolean,
    private val stateScope: CoroutineScope,
    private val bookSiblingsContext: BookSiblingsContext,
    private val colorCorrectionRepository: BookColorCorrectionRepository,
    val pageChangeFlow: SharedFlow<Unit>,
) {
    private val previewLoadScope = CoroutineScope(Dispatchers.Default.limitedParallelism(1) + SupervisorJob())
    val state = MutableStateFlow<LoadState<Unit>>(LoadState.Uninitialized)
    val expandImageSettings = MutableStateFlow(false)

    val booksState = MutableStateFlow<BookState?>(null)
    val series = MutableStateFlow<KomgaSeries?>(null)

    val readerType = MutableStateFlow(ReaderType.PAGED)
    val imageStretchToFit = MutableStateFlow(true)
    val cropBorders = MutableStateFlow(false)
    val readProgressPage = MutableStateFlow(1)

    val upsamplingMode = MutableStateFlow(UpsamplingMode.NEAREST)
    val downsamplingKernel = MutableStateFlow(ReduceKernel.NEAREST)
    val linearLightDownsampling = MutableStateFlow(false)
    val availableUpsamplingModes = availableUpsamplingModes()
    val availableDownsamplingKernels = availableReduceKernels()

    val flashOnPageChange = MutableStateFlow(false)
    val flashDuration = MutableStateFlow(100L)
    val flashEveryNPages = MutableStateFlow(1)
    val flashWith = MutableStateFlow(ReaderFlashColor.BLACK)

    val volumeKeysNavigation = MutableStateFlow(false)
    val pixelDensity = MutableStateFlow<Density?>(null)

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
        volumeKeysNavigation.value = readerSettingsRepository.getVolumeKeysNavigation().first()

        appNotifications.runCatchingToNotifications {
            state.value = LoadState.Loading
            val currentBooksState = booksState.value
            if (currentBooksState == null) state.value = LoadState.Loading
            val newBook = bookApi.getOne(bookId)

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

            val currentSeries = seriesApi.getOneSeries(newBook.seriesId)
            series.value = currentSeries
            readerType.value = when (currentSeries.metadata.readingDirection) {
                KomgaReadingDirection.LEFT_TO_RIGHT -> ReaderType.PAGED
                KomgaReadingDirection.RIGHT_TO_LEFT -> ReaderType.PAGED
                KomgaReadingDirection.WEBTOON -> ReaderType.CONTINUOUS
                KomgaReadingDirection.VERTICAL, null -> readerSettingsRepository.getReaderType().first()
            }

            state.value = LoadState.Success(Unit)
        }.onFailure { state.value = LoadState.Error(it) }

    }

    private suspend fun loadBookPages(bookId: KomgaBookId): List<PageMetadata> {
        val pages = bookApi.getBookPages(bookId)

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

    private suspend fun getNextBook(currentBookId: KomgaBookId): KomeliaBook? {
        return try {
            when (bookSiblingsContext) {
                is BookSiblingsContext.ReadList ->
                    readListApi.getBookSiblingNext(bookSiblingsContext.id, currentBookId)

                BookSiblingsContext.Series -> bookApi.getBookSiblingNext(currentBookId)
            }
        } catch (e: ClientRequestException) {
            if (e.response.status != NotFound) throw e
            else null
        }

    }

    private suspend fun getPreviousBook(currentBookId: KomgaBookId): KomeliaBook? {
        return try {
            when (bookSiblingsContext) {
                is BookSiblingsContext.ReadList ->
                    readListApi.getBookSiblingPrevious(bookSiblingsContext.id, currentBookId)

                BookSiblingsContext.Series -> bookApi.getBookSiblingPrevious(currentBookId)
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

            this.booksState.value = BookState(
                currentBook = booksState.nextBook,
                currentBookPages = booksState.nextBookPages,
                previousBook = booksState.currentBook,
                previousBookPages = booksState.currentBookPages,

                nextBook = nextBook,
                nextBookPages = nextBookPages
            )
            onProgressChange(1)
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

    suspend fun onProgressChange(page: Int) {
        readProgressPage.value = page

        if (markReadProgress) {
            appNotifications.runCatchingToNotifications {
                val currentBook = requireNotNull(booksState.value?.currentBook)
                bookApi.markReadProgress(
                    currentBook.id,
                    KomgaBookReadProgressUpdateRequest(page)
                )
            }
        }
    }

    fun onReaderTypeChange(type: ReaderType) {
        this.readerType.value = type
        stateScope.launch { readerSettingsRepository.putReaderType(type) }
    }

    fun onStretchToFitChange(stretch: Boolean) {
        imageStretchToFit.value = stretch
        stateScope.launch { readerSettingsRepository.putStretchToFit(stretch) }
    }

    fun onStretchToFitCycle() {
        val newValue = !imageStretchToFit.value
        imageStretchToFit.value = newValue
        stateScope.launch { readerSettingsRepository.putStretchToFit(newValue) }
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

    fun onColorCorrectionDisable() {
        stateScope.launch {
            booksState.value?.currentBook?.let { colorCorrectionRepository.deleteSettings(it.id) }
        }
    }

    fun onDispose() {
        currentBookId.value = null
        previewLoadScope.cancel()
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
    val currentBook: KomeliaBook,
    val currentBookPages: List<PageMetadata>,
    val previousBook: KomeliaBook?,
    val previousBookPages: List<PageMetadata>,
    val nextBook: KomeliaBook?,
    val nextBookPages: List<PageMetadata>,
)