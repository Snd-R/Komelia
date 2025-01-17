package io.github.snd_r.komelia.ui.reader.image

import androidx.compose.ui.unit.IntSize
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.navigator.Navigator
import io.github.snd_r.komelia.AppNotifications
import io.github.snd_r.komelia.image.BookImageLoader
import io.github.snd_r.komelia.image.ReaderImageFactory
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.strings.AppStrings
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.reader.image.ReaderType.CONTINUOUS
import io.github.snd_r.komelia.ui.reader.image.ReaderType.PAGED
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import snd.komga.client.book.KomgaBookClient
import snd.komga.client.book.KomgaBookId

private val cleanupScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

class ReaderViewModel(
    bookClient: KomgaBookClient,
    navigator: Navigator,
    appNotifications: AppNotifications,
    settingsRepository: CommonSettingsRepository,
    readerSettingsRepository: ImageReaderSettingsRepository,
    imageLoader: BookImageLoader,
    decoderDescriptor: Flow<PlatformDecoderDescriptor>,
    appStrings: Flow<AppStrings>,
    readerImageFactory: ReaderImageFactory,
    markReadProgress: Boolean,
    currentBookId: MutableStateFlow<KomgaBookId?>,
    val colorCorrectionIsActive: Flow<Boolean>
) : ScreenModel {
    val screenScaleState = ScreenScaleState()

    val readerState: ReaderState = ReaderState(
        bookClient = bookClient,
        navigator = navigator,
        appNotifications = appNotifications,
        settingsRepository = settingsRepository,
        readerSettingsRepository = readerSettingsRepository,
        decoderDescriptor = decoderDescriptor,
        currentBookId = currentBookId,
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
        readerImageFactory = readerImageFactory,
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
        screenScaleState = screenScaleState,
    )

    fun initialize(bookId: KomgaBookId) {
        screenModelScope.launch {
            val currentState = readerState.state.value
            if (currentState is LoadState.Success || currentState == LoadState.Loading) return@launch

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