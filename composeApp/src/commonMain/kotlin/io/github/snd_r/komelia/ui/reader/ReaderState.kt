package io.github.snd_r.komelia.ui.reader

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.navigator.Navigator
import coil3.request.ImageResult
import io.github.snd_r.komelia.AppNotification
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.MainScreen
import io.github.snd_r.komelia.ui.book.BookScreen
import io.github.snd_r.komga.book.KomgaBook
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookId
import io.github.snd_r.komga.book.KomgaBookReadProgressUpdateRequest
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode.Companion.NotFound
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
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
    private val markReadProgress: Boolean,
    private val coroutineScope: CoroutineScope,
) {
    val state = MutableStateFlow<LoadState<Unit>>(LoadState.Uninitialized)
    val bookState = MutableStateFlow<BookState?>(null)

    val allowUpsample = MutableStateFlow(false)
    val decoder = MutableStateFlow<SamplerType?>(null)

    suspend fun initialize(bookId: KomgaBookId) {
        allowUpsample.value = settingsRepository.getReaderUpsample().first()
        decoder.value = settingsRepository.getDecoderType().first()

        loadBook(bookId)
    }

    private suspend fun loadBook(bookId: KomgaBookId) {
        appNotifications.runCatchingToNotifications {
            state.value = LoadState.Loading
            val newBook = bookClient.getBook(bookId)
            val pages = bookClient.getBookPages(bookId)

            val bookPages = pages.map {
                val width = it.width
                val height = it.height
                PageMetadata(
                    bookId = bookId,
                    pageNumber = it.number,
                    size = if (width != null && height != null) IntSize(width, height) else null
                )
            }

            val prevBook = try {
                bookClient.getBookSiblingPrevious(bookId)
            } catch (e: ClientRequestException) {
                if (e.response.status != NotFound) throw e
                else null
            }
            val nextBook = try {
                bookClient.getBookSiblingNext(bookId)
            } catch (e: ClientRequestException) {
                if (e.response.status != NotFound) throw e
                else null
            }

            bookState.value = BookState(
                book = newBook,
                bookPages = bookPages,
                previousBook = prevBook,
                nextBook = nextBook
            )
            state.value = LoadState.Success(Unit)
        }.onFailure { state.value = LoadState.Error(it) }

    }

    fun loadNextBook() {
        val nextBook = bookState.value?.nextBook
        if (nextBook != null) {
            coroutineScope.launch {
                loadBook(nextBook.id)
                appNotifications.add(AppNotification.Normal("Loaded next book"))
            }
        } else {
            val currentBook = requireNotNull(bookState.value?.book)
            navigator replace MainScreen(BookScreen(currentBook.id))
        }
    }

    fun loadPreviousBook() {
        val prevBook = bookState.value?.previousBook
        if (prevBook != null) {
            coroutineScope.launch {
                loadBook(prevBook.id)
                appNotifications.add(AppNotification.Normal("Loaded previous book"))
            }
        } else
            appNotifications.add(AppNotification.Normal("You're at the beginning of the book"))
        return
    }


    fun onProgressChange(page: Int) {
        coroutineScope.launch {
//            readProgress.value = page
            if (markReadProgress) {
                val currentBook = requireNotNull(bookState.value?.book)
                bookClient.markReadProgress(currentBook.id, KomgaBookReadProgressUpdateRequest(page))
            }
        }
    }


    fun onAllowUpsampleChange(upsample: Boolean) {
        this.allowUpsample.value = upsample
        coroutineScope.launch { settingsRepository.putReaderUpsample(upsample) }

        val upsampleText = if (upsample) "Enabled"
        else "Disabled"
        appNotifications.add(AppNotification.Normal("$upsampleText upsample beyond image dimensions"))
    }


    fun onDecoderChange(type: SamplerType) {
        this.decoder.value = type
//        readerImageLoader.clearCache()
        coroutineScope.launch { settingsRepository.putDecoderType(type) }
    }
}

data class PageMetadata(
    val bookId: KomgaBookId,
    val pageNumber: Int,
    val size: IntSize?,
) {
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
    val book: KomgaBook,
    val bookPages: List<PageMetadata>,

    val previousBook: KomgaBook?,
    val nextBook: KomgaBook?,
)

data class PageSpread(
    val pages: List<Page>,
    val scaleState: PageSpreadScaleState? = null
) {
    companion object {
        val EMPTY_SPREAD = PageSpread(emptyList(), null)
    }
}

data class Page(
    val metadata: PageMetadata,
    val imageResult: ImageResult? = null
)

data class SpreadImageLoadJob(
    val pageJob: Deferred<List<Page>>,
    val hash: Int,
)
