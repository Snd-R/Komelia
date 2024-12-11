package io.github.snd_r.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import io.github.snd_r.komelia.fonts.UserFontsRepository
import io.github.snd_r.komelia.image.ReaderImageLoader
import io.github.snd_r.komelia.platform.AppWindowState
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.strings.EnStrings
import io.github.snd_r.komelia.ui.settings.decoder.DecoderSettingsViewModel
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.MangaJaNaiDownloader
import io.github.snd_r.komelia.updates.OnnxRuntimeInstaller
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory

class DesktopDependencyContainer(
    override val settingsRepository: CommonSettingsRepository,
    override val epubReaderSettingsRepository: EpubReaderSettingsRepository,
    override val imageReaderSettingsRepository: ImageReaderSettingsRepository,
    override val fontsRepository: UserFontsRepository,
    override val secretsRepository: SecretsRepository,
    override val komgaClientFactory: KomgaClientFactory,
    override val komfClientFactory: KomfClientFactory,
    override val appNotifications: AppNotifications,
    override val appUpdater: AppUpdater?,
    override val imageDecoderDescriptor: Flow<PlatformDecoderDescriptor>,
    override val imageLoader: ImageLoader,
    override val readerImageLoader: ReaderImageLoader,
    override val windowState: AppWindowState,
    val onnxRuntimeInstaller: OnnxRuntimeInstaller,
    val mangaJaNaiDownloader: MangaJaNaiDownloader
) : DependencyContainer {
    override val platformContext: PlatformContext = PlatformContext.INSTANCE
    override val appStrings = MutableStateFlow(EnStrings)
}

class DesktopViewModelFactory(private val dependencies: DesktopDependencyContainer) {
    fun getDecoderSettingsViewModel(): DecoderSettingsViewModel {
        return DecoderSettingsViewModel(
            settingsRepository = dependencies.settingsRepository,
            imageLoader = dependencies.imageLoader,
            onnxRuntimeInstaller = dependencies.onnxRuntimeInstaller,
            mangaJaNaiDownloader = dependencies.mangaJaNaiDownloader,
            appNotifications = dependencies.appNotifications,
            availableDecoders = dependencies.imageDecoderDescriptor,
        )
    }
}