package io.github.snd_r.komelia.ui.reader.image

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import coil3.ImageLoader
import coil3.PlatformContext
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.color.repository.BookColorCorrectionRepository
import io.github.snd_r.komelia.image.BookImageLoader
import io.github.snd_r.komelia.image.ReaderImageFactory
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.strings.AppStrings
import io.github.snd_r.komelia.ui.BookSiblingsContext
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.reader.image.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.image.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import io.github.snd_r.komelia.ui.settings.imagereader.OnnxRuntimeSettingsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import io.github.snd_r.komelia.image.KomeliaUpscaler
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.series.KomgaSeriesClient

private val cleanupScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

class ReaderViewModel(
    bookClient: KomgaBookClient,
    seriesClient: KomgaSeriesClient,
    readListClient: KomgaReadListClient,
    navigator: Navigator,
    appNotifications: AppNotifications,
    readerSettingsRepository: ImageReaderSettingsRepository,
    imageLoader: BookImageLoader,
    coilImageLoader: ImageLoader,
    coilContext: PlatformContext,
    appStrings: Flow<AppStrings>,
    readerImageFactory: ReaderImageFactory,
    markReadProgress: Boolean,
    currentBookId: MutableStateFlow<KomgaBookId?>,
    onnxRuntime: KomeliaUpscaler?,
    bookSiblingsContext: BookSiblingsContext,
    colorCorrectionRepository: BookColorCorrectionRepository,
    val colorCorrectionIsActive: Flow<Boolean>,
) : ScreenModel {
    val screenScaleState = ScreenScaleState()
    private val pageChangeFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val onnxRuntimeSettingsState = onnxRuntime?.let {
        OnnxRuntimeSettingsState(
            upscaler = onnxRuntime,
            onnxRuntimeInstaller = null,
            mangaJaNaiDownloader = null,
            appNotifications = appNotifications,
            settingsRepository = readerSettingsRepository,
            coroutineScope = screenModelScope,
        )
    }

    val readerState: ReaderState = ReaderState(
        bookClient = bookClient,
        seriesClient = seriesClient,
        readListClient = readListClient,
        navigator = navigator,
        appNotifications = appNotifications,
        readerSettingsRepository = readerSettingsRepository,
        currentBookId = currentBookId,
        markReadProgress = markReadProgress,
        stateScope = screenModelScope,
        bookSiblingsContext = bookSiblingsContext,
        coilImageLoader = coilImageLoader,
        coilContext = coilContext,
        colorCorrectionRepository = colorCorrectionRepository,
        pageChangeFlow = pageChangeFlow,
    )

    val pagedReaderState = PagedReaderState(
        cleanupScope = cleanupScope,
        readerState = readerState,
        appNotifications = appNotifications,
        settingsRepository = readerSettingsRepository,
        imageLoader = imageLoader,
        appStrings = appStrings,
        pageChangeFlow = pageChangeFlow,
        screenScaleState = screenScaleState,
    )
    val continuousReaderState = ContinuousReaderState(
        cleanupScope = cleanupScope,
        readerState = readerState,
        imageLoader = imageLoader,
        settingsRepository = readerSettingsRepository,
        notifications = appNotifications,
        appStrings = appStrings,
        readerImageFactory = readerImageFactory,
        pageChangeFlow = pageChangeFlow,
        screenScaleState = screenScaleState,
    )

    suspend fun initialize(bookId: KomgaBookId) {
        val currentState = readerState.state.value
        if (currentState is LoadState.Success || currentState == LoadState.Loading) return

        onnxRuntimeSettingsState?.initialize()
        readerState.initialize(bookId)
        screenScaleState.areaSize.takeWhile { it == IntSize.Zero }.collect()

        readerState.readerType.onEach {
            when (it) {
                PAGED -> {
                    continuousReaderState.stop()
                    pagedReaderState.initialize()
                }

                CONTINUOUS -> {
                    pagedReaderState.stop()
                    continuousReaderState.initialize()
                }
            }
        }.launchIn(screenModelScope)
    }

    override fun onDispose() {
        continuousReaderState.stop()
        pagedReaderState.stop()
        readerState.onDispose()
    }
}