package io.github.snd_r.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import io.github.snd_r.komelia.image.ReaderImageLoader
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.settings.DesktopReaderSettingsRepository
import io.github.snd_r.komelia.settings.DesktopSettingsRepository
import io.github.snd_r.komelia.settings.KeyringSecretsRepository
import io.github.snd_r.komelia.ui.settings.decoder.DecoderSettingsViewModel
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.MangaJaNaiDownloader
import io.github.snd_r.komelia.updates.OnnxRuntimeInstaller
import kotlinx.coroutines.flow.Flow
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory

class DesktopDependencyContainer(
    override val komgaClientFactory: KomgaClientFactory,
    override val appUpdater: AppUpdater,
    override val settingsRepository: DesktopSettingsRepository,
    override val readerSettingsRepository: DesktopReaderSettingsRepository,
    override val secretsRepository: KeyringSecretsRepository,
    override val imageLoader: ImageLoader,
    override val availableDecoders: Flow<List<PlatformDecoderDescriptor>>,
    override val readerImageLoader: ReaderImageLoader,
    override val appNotifications: AppNotifications,
    override val komfClientFactory: KomfClientFactory,
    val onnxRuntimeInstaller: OnnxRuntimeInstaller,
    val mangaJaNaiDownloader: MangaJaNaiDownloader
) : DependencyContainer {
    override val platformContext: PlatformContext = PlatformContext.INSTANCE
}

class DesktopViewModelFactory(private val dependencies: DesktopDependencyContainer) {
    fun getDecoderSettingsViewModel(): DecoderSettingsViewModel {
        return DecoderSettingsViewModel(
            settingsRepository = dependencies.settingsRepository,
            imageLoader = dependencies.imageLoader,
            onnxRuntimeInstaller = dependencies.onnxRuntimeInstaller,
            mangaJaNaiDownloader = dependencies.mangaJaNaiDownloader,
            appNotifications = dependencies.appNotifications,
            availableDecoders = dependencies.availableDecoders,
        )
    }
}