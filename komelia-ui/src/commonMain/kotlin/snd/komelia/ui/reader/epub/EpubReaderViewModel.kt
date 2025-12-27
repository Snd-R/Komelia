package snd.komelia.ui.reader.epub

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import snd.komelia.AppNotifications
import snd.komelia.AppWindowState
import snd.komelia.fonts.UserFontsRepository
import snd.komelia.komga.api.KomgaBookApi
import snd.komelia.komga.api.KomgaReadListApi
import snd.komelia.komga.api.KomgaSeriesApi
import snd.komelia.komga.api.model.KomeliaBook
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.settings.EpubReaderSettingsRepository
import snd.komelia.settings.model.EpubReaderType.KOMGA_EPUB
import snd.komelia.settings.model.EpubReaderType.TTSU_EPUB
import snd.komelia.ui.BookSiblingsContext
import snd.komelia.ui.LoadState
import snd.komelia.ui.platform.PlatformType
import snd.komga.client.book.KomgaBookId
import snd.webview.KomeliaWebview

class EpubReaderViewModel(
    private val bookId: KomgaBookId,
    private val book: KomeliaBook?,
    private val markReadProgress: Boolean,
    private val bookApi: KomgaBookApi,
    private val seriesApi: KomgaSeriesApi,
    private val readListApi: KomgaReadListApi,
//    private val ktor: HttpClient,
    private val settingsRepository: CommonSettingsRepository,
    private val epubSettingsRepository: EpubReaderSettingsRepository,
    private val fontsRepository: UserFontsRepository,
    private val notifications: AppNotifications,
    private val windowState: AppWindowState,
    private val platformType: PlatformType,
    private val bookSiblingsContext: BookSiblingsContext,
) : StateScreenModel<LoadState<EpubReaderState>>(LoadState.Uninitialized) {

    suspend fun initialize(navigator: Navigator) {
        when (val state = state.value) {
            LoadState.Loading, is LoadState.Error -> {}
            is LoadState.Success<EpubReaderState> -> state.value.initialize(navigator)
            LoadState.Uninitialized -> {

                when (epubSettingsRepository.getReaderType().first()) {
                    KOMGA_EPUB -> {
                        val komgaState = KomgaEpubReaderState(
                            bookId = bookId,
                            book = book,
                            bookApi = bookApi,
                            seriesApi = seriesApi,
                            readListApi = readListApi,
                            settingsRepository = settingsRepository,
                            notifications = notifications,
//                            ktor = ktor,
                            markReadProgress = markReadProgress,
                            epubSettingsRepository = epubSettingsRepository,
                            windowState = windowState,
                            platformType = platformType,
                            coroutineScope = screenModelScope,
                            bookSiblingsContext = bookSiblingsContext,
                        )
                        komgaState.initialize(navigator)
                        when (val res = komgaState.state.value) {
                            is LoadState.Error -> mutableState.value = LoadState.Error(res.exception)
                            is LoadState.Success<Unit> -> mutableState.value = LoadState.Success(komgaState)
                            LoadState.Loading, LoadState.Uninitialized -> LoadState.Loading
                        }
                    }

                    TTSU_EPUB -> {
                        val ttsuState = TtsuReaderState(
                            bookId = bookId,
                            book = book,
                            bookApi = bookApi,
                            notifications = notifications,
//                            ktor = ktor,
                            markReadProgress = markReadProgress,
                            settingsRepository = settingsRepository,
                            epubSettingsRepository = epubSettingsRepository,
                            fontsRepository = fontsRepository,
                            windowState = windowState,
                            platformType = platformType,
                            coroutineScope = screenModelScope,
                            bookSiblingsContext = bookSiblingsContext,
                        )
                        ttsuState.initialize(navigator)
                        when (val res = ttsuState.state.value) {
                            is LoadState.Error -> mutableState.value = LoadState.Error(res.exception)
                            is LoadState.Success<Unit> -> mutableState.value = LoadState.Success(ttsuState)
                            LoadState.Loading, LoadState.Uninitialized -> LoadState.Loading
                        }
                    }
                }
            }
        }
    }
}

interface EpubReaderState {
    val state: StateFlow<LoadState<Unit>>
    val book: StateFlow<KomeliaBook?>
    suspend fun initialize(navigator: Navigator)
    fun onWebviewCreated(webview: KomeliaWebview)
    fun onBackButtonPress()
    fun closeWebview()
}