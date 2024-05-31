package io.github.snd_r.komelia.ui.reader

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.platform.CommonParcelable
import io.github.snd_r.komelia.platform.CommonParcelize
import io.github.snd_r.komelia.platform.CommonParcelizeRawValue
import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.reader.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.book.KomgaBookReadProgressUpdateRequest
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode.Companion.NotFound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

typealias SpreadIndex = Int

class ReaderState(
    private val bookClient: KomgaBookClient,
    private val navigator: Navigator,
    private val appNotifications: AppNotifications,
    private val settingsRepository: SettingsRepository,
    private val readerSettingsRepository: ReaderSettingsRepository,
    private val markReadProgress: Boolean,
    private val stateScope: CoroutineScope,
) : ScreenModel {
    val state = MutableStateFlow<LoadState<Unit>>(LoadState.Uninitialized)
    val readerType = MutableStateFlow(CONTINUOUS)
    val decoder = MutableStateFlow<SamplerType?>(null)

    val booksState = MutableStateFlow<BookState?>(null)
    val readProgressPage = MutableStateFlow(1)

    suspend fun initialize(bookId: KomgaBookId) {
        decoder.value = settingsRepository.getDecoderType().first()
        readerType.value = readerSettingsRepository.getReaderType().first()

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

            this.booksState.value = BookState(
                currentBook = booksState.nextBook,
                currentBookPages = booksState.nextBookPages,
                previousBook = booksState.currentBook,
                previousBookPages = booksState.currentBookPages,

                nextBook = nextBook,
                nextBookPages = nextBookPages
            )

            val bookProgress = booksState.nextBook.readProgress
            readProgressPage.value = when {
                bookProgress == null || bookProgress.completed -> 1
                else -> bookProgress.page
            }
        } else {
            navigator replace MainScreen(SeriesScreen(booksState.currentBook.seriesId))
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
            val previousBookPages = if (previousBook != null) loadBookPages(previousBook.id) else emptyList()

            this.booksState.value = BookState(
                currentBook = booksState.previousBook,
                currentBookPages = booksState.previousBookPages,
                nextBook = booksState.currentBook,
                nextBookPages = booksState.currentBookPages,

                previousBook = previousBook,
                previousBookPages = previousBookPages,
            )
            val bookProgress = booksState.previousBook.readProgress
            readProgressPage.value = when {
                bookProgress == null || bookProgress.completed -> 1
                else -> bookProgress.page
            }
        } else
            appNotifications.add(AppNotification.Normal("You're at the beginning of the book"))
        return
    }

    suspend fun onProgressChange(page: Int) {
        if (markReadProgress) {
            val currentBook = requireNotNull(booksState.value?.currentBook)
            stateScope.launch {
                bookClient.markReadProgress(currentBook.id, KomgaBookReadProgressUpdateRequest(page))
            }
        }

        readProgressPage.value = page
    }

    fun onDecoderChange(type: SamplerType) {
        this.decoder.value = type
        stateScope.launch { settingsRepository.putDecoderType(type) }
    }

    fun onReaderTypeChange(type: ReaderType) {
        this.readerType.value = type
        stateScope.launch { readerSettingsRepository.putReaderType(type) }
    }
}

@CommonParcelize
data class PageMetadata(

    val bookId: @CommonParcelizeRawValue KomgaBookId,
    val pageNumber: Int,
    val size: @CommonParcelizeRawValue IntSize?,
) : CommonParcelable {
    fun contentSizeForArea(maxPageSize: IntSize): IntSize {
        val pageSize = size ?: return maxPageSize

        val bestRatio = (maxPageSize.width.toDouble() / pageSize.width)
            .coerceAtMost(maxPageSize.height.toDouble() / pageSize.height)

        val scaledSize = IntSize(
            width = (pageSize.width * bestRatio).roundToInt(),
            height = (pageSize.height * bestRatio).roundToInt()
        )

        return scaledSize
    }

    fun isLandscape(): Boolean {
        if (size == null) return false
        return size.width > size.height
    }
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