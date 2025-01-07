package io.github.snd_r.komelia

import coil3.ImageLoader
import coil3.PlatformContext
import io.github.snd_r.komelia.color.repository.BookColorCorrectionRepository
import io.github.snd_r.komelia.fonts.UserFontsRepository
import io.github.snd_r.komelia.image.BookImageLoader
import io.github.snd_r.komelia.image.ReaderImageFactory
import io.github.snd_r.komelia.platform.AppWindowState
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.color.repository.ColorCurvePresetRepository
import io.github.snd_r.komelia.color.repository.ColorLevelsPresetRepository
import io.github.snd_r.komelia.image.processing.ColorCorrectionStep
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.settings.SecretsRepository
import io.github.snd_r.komelia.strings.AppStrings
import io.github.snd_r.komelia.updates.AppUpdater
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.ImageDecoder
import snd.komf.client.KomfClientFactory
import snd.komga.client.KomgaClientFactory

interface DependencyContainer {
    val settingsRepository: CommonSettingsRepository
    val epubReaderSettingsRepository: EpubReaderSettingsRepository
    val imageReaderSettingsRepository: ImageReaderSettingsRepository
    val fontsRepository: UserFontsRepository
    val colorCurvesPresetsRepository: ColorCurvePresetRepository
    val colorLevelsPresetRepository: ColorLevelsPresetRepository
    val bookColorCorrectionRepository: BookColorCorrectionRepository
    val secretsRepository: SecretsRepository

    val komgaClientFactory: KomgaClientFactory
    val komfClientFactory: KomfClientFactory
    val appNotifications: AppNotifications
    val appUpdater: AppUpdater?
    val imageDecoderDescriptor: Flow<PlatformDecoderDescriptor>
    val imageDecoder: ImageDecoder
    val coilImageLoader: ImageLoader
    val bookImageLoader: BookImageLoader
    val readerImageFactory: ReaderImageFactory
    val platformContext: PlatformContext
    val windowState: AppWindowState
    val appStrings: StateFlow<AppStrings>
    val colorCorrectionStep: ColorCorrectionStep
}
