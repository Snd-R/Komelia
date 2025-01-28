package io.github.snd_r.komelia.ui.reader.image

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.image.ReaderImage
import io.github.snd_r.komelia.image.ReaderImage.PageId
import io.github.snd_r.komelia.platform.CommonParcelable
import io.github.snd_r.komelia.platform.CommonParcelize
import io.github.snd_r.komelia.platform.CommonParcelizeRawValue
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.oneshot.OneshotScreen
import io.github.snd_r.komelia.ui.reader.image.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode.Companion.NotFound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.book.KomgaBookReadProgressUpdateRequest

typealias SpreadIndex = Int

class ReaderState(
    private val bookClient: KomgaBookClient,
    private val navigator: Navigator,
    private val appNotifications: AppNotifications,
    private val settingsRepository: CommonSettingsRepository,
    private val readerSettingsRepository: ImageReaderSettingsRepository,
    private val decoderDescriptor: Flow<PlatformDecoderDescriptor>,
    private val currentBookId: MutableStateFlow<KomgaBookId?>,
    private val markReadProgress: Boolean,
    private val stateScope: CoroutineScope,
) {
    val state = MutableStateFlow<LoadState<Unit>>(LoadState.Uninitialized)
    val expandImageSettings = MutableStateFlow(false)

    val currentDecoderDescriptor = MutableStateFlow<PlatformDecoderDescriptor?>(null)
    val decoderSettings = MutableStateFlow<PlatformDecoderSettings?>(null)
    val readerType = MutableStateFlow(CONTINUOUS)
    val imageStretchToFit = MutableStateFlow(true)
    val cropBorders = MutableStateFlow(false)
    val booksState = MutableStateFlow<BookState?>(null)
    val readProgressPage = MutableStateFlow(1)

    suspend fun initialize(bookId: KomgaBookId) {

        decoderSettings.value = settingsRepository.getDecoderSettings().first()
        readerType.value = readerSettingsRepository.getReaderType().first()
        imageStretchToFit.value = readerSettingsRepository.getStretchToFit().first()
        cropBorders.value = readerSettingsRepository.getCropBorders().first()

        decoderDescriptor.onEach { currentDecoderDescriptor.value = it }.launchIn(stateScope)
        loadBook(bookId)
    }

    private suspend fun loadBook(bookId: KomgaBookId) {
        appNotifications.runCatchingToNotifications {
            val currentBooksState = booksState.value
            if (currentBooksState == null) state.value = LoadState.Loading
            val newBook = bookClient.getBook(bookId)

            val bookPages = loadBookPages(newBook.id)

            val prevBook = try {
                bookClient.getBookSiblingPrevious(bookId)
            } catch (e: ClientRequestException) {
                if (e.response.status != NotFound) throw e
                else null
            }
            val prevBookPages = if (prevBook != null) loadBookPages(prevBook.id) else emptyList()
            val nextBook = try {
                bookClient.getBookSiblingNext(bookId)
            } catch (e: ClientRequestException) {
                if (e.response.status != NotFound) throw e
                else null
            }
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

    suspend fun loadNextBook() {
        val booksState = requireNotNull(booksState.value)
        if (booksState.nextBook != null) {
            val nextBook = try {
                bookClient.getBookSiblingNext(booksState.nextBook.id)
            } catch (e: ClientRequestException) {
                if (e.response.status != NotFound) throw e
                else null
            }
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
                if (booksState.currentBook.oneshot) OneshotScreen(booksState.currentBook)
                else SeriesScreen(booksState.currentBook.seriesId)
            )
        }
    }

    suspend fun loadPreviousBook() {
        val booksState = requireNotNull(booksState.value)
        if (booksState.previousBook != null) {
            val previousBook = try {
                bookClient.getBookSiblingPrevious(booksState.previousBook.id)
            } catch (e: ClientRequestException) {
                if (e.response.status != NotFound) throw e
                else null
            }
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

    fun onUpscaleMethodChange(upscaleOption: UpscaleOption) {
        val currentDecoder = requireNotNull(this.decoderSettings.value)
        val newDecoder = currentDecoder.copy(upscaleOption = upscaleOption)
        this.decoderSettings.value = newDecoder
        stateScope.launch { settingsRepository.putDecoderSettings(newDecoder) }
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

sealed interface ReaderImageResult {
    val image: ReaderImage?

    data class Success(override val image: ReaderImage) : ReaderImageResult
    data class Error(val throwable: Throwable) : ReaderImageResult {
        override val image: ReaderImage? = null
    }
}
