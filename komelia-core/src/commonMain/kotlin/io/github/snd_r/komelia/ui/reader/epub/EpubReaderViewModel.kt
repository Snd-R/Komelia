package io.github.snd_r.komelia.ui.reader.epub

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.fonts.UserFontsRepository
import io.github.snd_r.komelia.platform.AppWindowState
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType.KOMGA_EPUB
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType.TTSU_EPUB
import io.ktor.client.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import snd.komga.client.book.KomgaBook
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import snd.komga.client.readlist.KomgaReadListClient
import snd.komga.client.series.KomgaSeriesClient
import snd.webview.KomeliaWebview

class EpubReaderViewModel(
    private val bookId: KomgaBookId,
    private val book: KomgaBook?,
    private val markReadProgress: Boolean,
    private val bookClient: KomgaBookClient,
    private val seriesClient: KomgaSeriesClient,
    private val readListClient: KomgaReadListClient,
    private val ktor: HttpClient,
    private val settingsRepository: CommonSettingsRepository,
    private val epubSettingsRepository: EpubReaderSettingsRepository,
    private val fontsRepository: UserFontsRepository,
    private val notifications: AppNotifications,
    private val windowState: AppWindowState,
    private val platformType: PlatformType
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
                            bookClient = bookClient,
                            seriesClient = seriesClient,
                            readListClient = readListClient,
                            settingsRepository = settingsRepository,
                            notifications = notifications,
                            ktor = ktor,
                            markReadProgress = markReadProgress,
                            epubSettingsRepository = epubSettingsRepository,
                            windowState = windowState,
                            platformType = platformType,
                            coroutineScope = screenModelScope,
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
                            bookClient = bookClient,
                            notifications = notifications,
                            ktor = ktor,
                            markReadProgress = markReadProgress,
                            settingsRepository = settingsRepository,
                            epubSettingsRepository = epubSettingsRepository,
                            fontsRepository = fontsRepository,
                            windowState = windowState,
                            platformType = platformType,
                            coroutineScope = screenModelScope,
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
    val book: StateFlow<KomgaBook?>
    suspend fun initialize(navigator: Navigator)
    fun onWebviewCreated(webview: KomeliaWebview)
    fun closeWebview()
}