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
import io.github.snd_r.komelia.updates.AppUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory

class AndroidDependencyContainer(
    override val settingsRepository: CommonSettingsRepository,
    override val epubReaderSettingsRepository: EpubReaderSettingsRepository,
    override val imageReaderSettingsRepository: ImageReaderSettingsRepository,
    override val fontsRepository: UserFontsRepository,
    override val secretsRepository: SecretsRepository,
    override val komgaClientFactory: KomgaClientFactory,
    override val komfClientFactory: KomfClientFactory,
    override val appUpdater: AppUpdater?,
    override val imageDecoderDescriptor: Flow<PlatformDecoderDescriptor>,
    override val imageLoader: ImageLoader,
    override val readerImageLoader: ReaderImageLoader,
    override val platformContext: PlatformContext,
    override val windowState: AppWindowState,
) : DependencyContainer {
    override val appNotifications = AppNotifications()
    override val appStrings = MutableStateFlow(EnStrings)
}
