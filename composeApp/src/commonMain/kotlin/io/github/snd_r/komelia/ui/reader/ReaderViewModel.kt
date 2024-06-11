package io.github.snd_r.komelia.ui.reader

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import coil3.ImageLoader
import coil3.PlatformContext
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.settings.ReaderSettingsRepository
import io.github.snd_r.komelia.settings.SettingsRepository
import io.github.snd_r.komelia.ui.reader.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komga.book.KomgaBookClient
import io.github.snd_r.komga.book.KomgaBookId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

class ReaderViewModel(
    imageLoader: ImageLoader,
    imageLoaderContext: PlatformContext,
    bookClient: KomgaBookClient,
    navigator: Navigator,
    appNotifications: AppNotifications,
    settingsRepository: SettingsRepository,
    readerSettingsRepository: ReaderSettingsRepository,
    availableDecoders: Flow<List<PlatformDecoderDescriptor>>,
    markReadProgress: Boolean,
) : ScreenModel {
    val screenScaleState = ScreenScaleState()

    val readerState: ReaderState = ReaderState(
        bookClient = bookClient,
        navigator = navigator,
        appNotifications = appNotifications,
        settingsRepository = settingsRepository,
        readerSettingsRepository = readerSettingsRepository,
        availableDecoders = availableDecoders,
        markReadProgress = markReadProgress,
        stateScope = screenModelScope
    )

    val pagedReaderState = PagedReaderState(
        imageLoader = imageLoader,
        imageLoaderContext = imageLoaderContext,
        readerState = readerState,
        appNotifications = appNotifications,
        settingsRepository = readerSettingsRepository,
        screenScaleState = screenScaleState
    )
    val continuousReaderState = ContinuousReaderState(
        imageLoader = imageLoader,
        imageLoaderContext = imageLoaderContext,
        readerState = readerState,
        settingsRepository = readerSettingsRepository,
        notifications = appNotifications,
        screenScaleState = screenScaleState,
    )

    fun initialize(bookId: KomgaBookId) {
        screenModelScope.launch {
            readerState.initialize(bookId)
            screenScaleState.areaSize.takeWhile { it == IntSize.Zero }.collect()

            readerState.readerType.collect {
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
            }
        }
    }

    override fun onDispose() {
        continuousReaderState.stop()
        pagedReaderState.stop()
    }
}
