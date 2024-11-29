package io.github.snd_r.komelia.ui.reader

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.image.ReaderImageLoader
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.strings.Strings
import io.github.snd_r.komelia.ui.reader.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository

private val cleanupScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

class ReaderViewModel(
    bookClient: KomgaBookClient,
    navigator: Navigator,
    appNotifications: AppNotifications,
    settingsRepository: CommonSettingsRepository,
    readerSettingsRepository: ImageReaderSettingsRepository,
    imageLoader: ReaderImageLoader,
    decoderDescriptor: Flow<PlatformDecoderDescriptor>,
    appStrings: Flow<Strings>,
    markReadProgress: Boolean,
) : ScreenModel {
    val screenScaleState = ScreenScaleState()

    val readerState: ReaderState = ReaderState(
        bookClient = bookClient,
        navigator = navigator,
        appNotifications = appNotifications,
        settingsRepository = settingsRepository,
        readerSettingsRepository = readerSettingsRepository,
        decoderDescriptor = decoderDescriptor,
        markReadProgress = markReadProgress,
        stateScope = screenModelScope,
    )

    val pagedReaderState = PagedReaderState(
        cleanupScope = cleanupScope,
        readerState = readerState,
        appNotifications = appNotifications,
        settingsRepository = readerSettingsRepository,
        imageLoader = imageLoader,
        appStrings = appStrings,
        screenScaleState = screenScaleState,
    )
    val continuousReaderState = ContinuousReaderState(
        cleanupScope = cleanupScope,
        readerState = readerState,
        imageLoader = imageLoader,
        settingsRepository = readerSettingsRepository,
        notifications = appNotifications,
        appStrings = appStrings,
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
        readerState.onDispose()
    }
}