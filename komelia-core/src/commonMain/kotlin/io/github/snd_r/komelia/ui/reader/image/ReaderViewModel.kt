package io.github.snd_r.komelia.ui.reader.image

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import coil3.ImageLoader
import coil3.PlatformContext
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.color.repository.BookColorCorrectionRepository
import io.github.snd_r.komelia.image.BookImageLoader
import io.github.snd_r.komelia.image.KomeliaPanelDetector
import io.github.snd_r.komelia.image.KomeliaUpscaler
import io.github.snd_r.komelia.image.ReaderImageFactory
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.strings.AppStrings
import io.github.snd_r.komelia.ui.BookSiblingsContext
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.reader.image.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.image.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.image.ReaderType.PANELS
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.image.panels.PanelsReaderState
import io.github.snd_r.komelia.ui.settings.imagereader.onnxruntime.OnnxRuntimeSettingsState
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
import snd.komelia.onnxruntime.OnnxRuntime
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.series.KomgaSeriesClient

private val cleanupScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
private val logger = KotlinLogging.logger { }

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
//    onnxRuntime: OnnxRuntime?,
//    upscaler: KomeliaUpscaler?,
//    panelDetector: KomeliaPanelDetector,
    bookSiblingsContext: BookSiblingsContext,
    colorCorrectionRepository: BookColorCorrectionRepository,
    private val onnxRuntime: OnnxRuntime?,
    private val panelDetector: KomeliaPanelDetector?,
    private val upscaler: KomeliaUpscaler?,
    val colorCorrectionIsActive: Flow<Boolean>,
) : ScreenModel {
    val screenScaleState = ScreenScaleState()
    private val pageChangeFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val onnxRuntimeSettingsState = upscaler?.let {
        OnnxRuntimeSettingsState(
            onnxRuntimeInstaller = null,
            onnxModelDownloader = null,
            onnxRuntime = onnxRuntime,
            upscaler = upscaler,
            panelDetector = panelDetector,
//            appNotifications = appNotifications,
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
    val panelsReaderState = panelDetector?.let { panelDetector ->
        if (!panelDetector.isAvailable.value) null
        else
            PanelsReaderState(
                cleanupScope = cleanupScope,
                readerState = readerState,
                appNotifications = appNotifications,
                settingsRepository = readerSettingsRepository,
                imageLoader = imageLoader,
                appStrings = appStrings,
                pageChangeFlow = pageChangeFlow,
                screenScaleState = screenScaleState,
                onnxRuntimeRfDetr = panelDetector
            )
    }
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
            stopAllReaderModeStates()
            when (it) {
                PAGED -> pagedReaderState.initialize()
                CONTINUOUS -> continuousReaderState.initialize()
                PANELS -> {
                    if (panelsReaderState == null) {
                        logger.warn { "onnx runtime was not provided. Falling back to paged reader" }
                        readerState.onReaderTypeChange(PAGED)
                    } else {
                        panelsReaderState.initialize()
                    }
                }
            }
        }.launchIn(screenModelScope)
    }

    private fun stopAllReaderModeStates() {
        pagedReaderState.stop()
        continuousReaderState.stop()
        panelsReaderState?.stop()

    }

    override fun onDispose() {
        stopAllReaderModeStates()
        readerState.onDispose()
        panelDetector?.closeCurrentSession()
        upscaler?.closeCurrentSession()
    }
}