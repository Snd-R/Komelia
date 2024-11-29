package io.github.snd_r.komelia

import cafe.adriel.lyricist.Lyricist
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
import io.github.snd_r.komelia.strings.Strings
import io.github.snd_r.komelia.updates.AppUpdater
import kotlinx.coroutines.flow.Flow
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory

interface DependencyContainer {
    val settingsRepository: CommonSettingsRepository
    val epubReaderSettingsRepository: EpubReaderSettingsRepository
    val imageReaderSettingsRepository: ImageReaderSettingsRepository
    val fontsRepository: UserFontsRepository
    val secretsRepository: SecretsRepository
    val komgaClientFactory: KomgaClientFactory
    val komfClientFactory: KomfClientFactory
    val appNotifications: AppNotifications
    val appUpdater: AppUpdater?
    val imageDecoderDescriptor: Flow<PlatformDecoderDescriptor>
    val imageLoader: ImageLoader
    val readerImageLoader: ReaderImageLoader
    val platformContext: PlatformContext
    val windowState: AppWindowState
    val lyricist: Lyricist<Strings>
}
