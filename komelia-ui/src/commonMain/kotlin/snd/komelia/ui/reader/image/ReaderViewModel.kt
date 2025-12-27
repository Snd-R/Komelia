package snd.komelia.ui.reader.image

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import io.github.oshai.kotlinlogging.KotlinLogging
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
import snd.komelia.AppNotifications
import snd.komelia.color.repository.BookColorCorrectionRepository
import snd.komelia.image.BookImageLoader
import snd.komelia.image.KomeliaPanelDetector
import snd.komelia.image.KomeliaUpscaler
import snd.komelia.image.ReaderImageFactory
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.onnxruntime.OnnxRuntime
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.settings.model.ReaderType.CONTINUOUS
import snd.komelia.settings.model.ReaderType.PAGED
import snd.komelia.settings.model.ReaderType.PANELS
import snd.komelia.ui.BookSiblingsContext
import snd.komelia.ui.LoadState
import snd.komelia.ui.reader.image.continuous.ContinuousReaderState
import snd.komelia.ui.reader.image.paged.PagedReaderState
import snd.komelia.ui.reader.image.panels.PanelsReaderState
import snd.komelia.ui.settings.imagereader.onnxruntime.OnnxRuntimeSettingsState
import snd.komelia.ui.strings.AppStrings
import snd.komga.client.book.KomgaBookId

private val cleanupScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
private val logger = KotlinLogging.logger { }

class ReaderViewModel(
    bookApi: KomgaBookApi,
    seriesApi: KomgaSeriesApi,
    readListApi: KomgaReadListApi,
    navigator: Navigator,
    appNotifications: AppNotifications,
    readerSettingsRepository: ImageReaderSettingsRepository,
    imageLoader: BookImageLoader,
    appStrings: Flow<AppStrings>,
    readerImageFactory: ReaderImageFactory,
    markReadProgress: Boolean,
    currentBookId: MutableStateFlow<KomgaBookId?>,
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
            settingsRepository = readerSettingsRepository,
            coroutineScope = screenModelScope,
        )
    }

    val readerState: ReaderState = ReaderState(
        bookApi = bookApi,
        seriesApi = seriesApi,
        readListApi = readListApi,
        navigator = navigator,
        appNotifications = appNotifications,
        readerSettingsRepository = readerSettingsRepository,
        currentBookId = currentBookId,
        markReadProgress = markReadProgress,
        stateScope = screenModelScope,
        bookSiblingsContext = bookSiblingsContext,
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