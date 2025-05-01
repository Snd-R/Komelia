package io.github.snd_r.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import io.github.snd_r.komelia.color.repository.BookColorCorrectionRepository
import io.github.snd_r.komelia.color.repository.ColorCurvePresetRepository
import io.github.snd_r.komelia.color.repository.ColorLevelsPresetRepository
import io.github.snd_r.komelia.fonts.UserFontsRepository
import io.github.snd_r.komelia.image.BookImageLoader
import io.github.snd_r.komelia.image.ReaderImageFactory
import io.github.snd_r.komelia.image.processing.ColorCorrectionStep
import io.github.snd_r.komelia.platform.AppWindowState
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.settings.KomfSettingsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.strings.EnStrings
import io.github.snd_r.komelia.updates.AppUpdater
import io.github.snd_r.komelia.updates.MangaJaNaiDownloader
import io.github.snd_r.komelia.updates.OnnxRuntimeInstaller
import kotlinx.coroutines.flow.MutableStateFlow
import snd.komelia.image.ImageDecoder
import snd.komelia.image.OnnxRuntime
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory

class DesktopDependencyContainer(
    override val settingsRepository: CommonSettingsRepository,
    override val epubReaderSettingsRepository: EpubReaderSettingsRepository,
    override val imageReaderSettingsRepository: ImageReaderSettingsRepository,
    override val fontsRepository: UserFontsRepository,
    override val colorCurvesPresetsRepository: ColorCurvePresetRepository,
    override val colorLevelsPresetRepository: ColorLevelsPresetRepository,
    override val bookColorCorrectionRepository: BookColorCorrectionRepository,
    override val secretsRepository: SecretsRepository,
    override val komfSettingsRepository: KomfSettingsRepository,

    override val komgaClientFactory: KomgaClientFactory,
    override val komfClientFactory: KomfClientFactory,
    override val appNotifications: AppNotifications,
    override val appUpdater: AppUpdater?,
    override val imageDecoder: ImageDecoder,
    override val coilImageLoader: ImageLoader,
    override val bookImageLoader: BookImageLoader,
    override val readerImageFactory: ReaderImageFactory,
    override val windowState: AppWindowState,
    override val colorCorrectionStep: ColorCorrectionStep,
    override val onnxRuntimeInstaller: OnnxRuntimeInstaller,
    override val mangaJaNaiDownloader: MangaJaNaiDownloader,
    override val onnxRuntime: OnnxRuntime?,
) : DependencyContainer {
    override val platformContext: PlatformContext = PlatformContext.INSTANCE
    override val appStrings = MutableStateFlow(EnStrings)
}