package snd.komelia

import snd.komelia.color.repository.BookColorCorrectionRepository
import snd.komelia.color.repository.ColorCurvePresetRepository
import snd.komelia.color.repository.ColorLevelsPresetRepository
import snd.komelia.fonts.UserFontsRepository
import snd.komelia.homefilters.HomeScreenFilterRepository
import snd.komelia.offline.OfflineModule
import snd.komelia.settings.CommonSettingsRepository
import snd.komelia.settings.EpubReaderSettingsRepository
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.settings.KomfSettingsRepository
import snd.komelia.settings.SecretsRepository

class CoreModule(
    val appRepositories: AppRepositories,
    private val offlineModule: OfflineModule
) {
}

data class AppRepositories(
    val settingsRepository: CommonSettingsRepository,
    val epubReaderSettingsRepository: EpubReaderSettingsRepository,
    val imageReaderSettingsRepository: ImageReaderSettingsRepository,
    val fontsRepository: UserFontsRepository,
    val colorCurvesPresetsRepository: ColorCurvePresetRepository,
    val colorLevelsPresetRepository: ColorLevelsPresetRepository,
    val bookColorCorrectionRepository: BookColorCorrectionRepository,
    val secretsRepository: SecretsRepository,
    val komfSettingsRepository: KomfSettingsRepository,
    val homeScreenFilterRepository: HomeScreenFilterRepository,
)
