package snd.komelia.db.settings

import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.PageDisplayLayout
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.datetime.Instant
import snd.komelia.db.AppSettingsQueries

class SettingsRepository(private val queries: AppSettingsQueries) {
    fun get(): AppSettings? {
        return queries.get().executeAsOneOrNull()?.let { fromRecord(it) }
    }

    fun save(settings: AppSettings) {
        queries.save(toRecord(settings))
    }

    private fun fromRecord(settings: snd.komelia.db.AppSettings): AppSettings {
        return AppSettings(
            username = settings.username,
            serverUrl = settings.serverUrl,
            cardWidth = settings.cardWidth.toInt(),
            seriesPageLoadSize = settings.seriesPageLoadSize.toInt(),
            bookPageLoadSize = settings.bookPageLoadSize.toInt(),
            bookListLayout = BooksLayout.valueOf(settings.bookListLayout),
            appTheme = AppTheme.valueOf(settings.appTheme),
            checkForUpdatesOnStartup = settings.checkForUpdatesOnStartup,
            updateLastCheckedTimestamp = settings.updateLastCheckedTimestamp?.let { Instant.fromEpochMilliseconds(it) },
            updateLastCheckedReleaseVersion = settings.updateLastCheckedReleaseVersion?.let { AppVersion.fromString(it) },
            updateDismissedVersion = settings.updateDismissedVersion?.let { AppVersion.fromString(it) },
            upscaleOption = settings.upscaleOption,
            downscaleOption = settings.downscaleOption,
            onnxModelsPath = settings.onnxModelsPath,
            onnxRuntimeDeviceId = settings.onnxRuntimeDeviceId.toInt(),
            onnxRuntimeTileSize = settings.onnxRuntimeTileSize.toInt(),
            readerType = ReaderType.valueOf(settings.readerType),
            stretchToFit = settings.stretchToFit,
            pagedScaleType = LayoutScaleType.valueOf(settings.pagedScaleType),
            pagedReadingDirection = PagedReaderState.ReadingDirection.valueOf(settings.pagedReadingDirection),
            pagedPageLayout = PageDisplayLayout.valueOf(settings.pagedPageLayout),
            continuousReadingDirection = ContinuousReaderState.ReadingDirection.valueOf(settings.continuousReadingDirection),
            continuousPadding = settings.continuousPadding.toFloat(),
            continuousPageSpacing = settings.continuousPageSpacing.toInt(),
            komfEnabled = settings.komfEnabled,
            komfMode = KomfMode.valueOf(settings.komfMode),
            komfRemoteUrl = settings.komfRemoteUrl
        )
    }

    private fun toRecord(settings: AppSettings): snd.komelia.db.AppSettings {
        return snd.komelia.db.AppSettings(
            version = 1,
            username = settings.username,
            serverUrl = settings.serverUrl,
            cardWidth = settings.cardWidth.toLong(),
            seriesPageLoadSize = settings.seriesPageLoadSize.toLong(),
            bookPageLoadSize = settings.bookPageLoadSize.toLong(),
            bookListLayout = settings.bookListLayout.name,
            appTheme = settings.appTheme.name,
            checkForUpdatesOnStartup = settings.checkForUpdatesOnStartup,
            updateLastCheckedTimestamp = settings.updateLastCheckedTimestamp?.toEpochMilliseconds(),
            updateLastCheckedReleaseVersion = settings.updateLastCheckedReleaseVersion?.toString(),
            updateDismissedVersion = settings.updateDismissedVersion?.toString(),
            upscaleOption = settings.upscaleOption,
            downscaleOption = settings.downscaleOption,
            onnxModelsPath = settings.onnxModelsPath,
            onnxRuntimeDeviceId = settings.onnxRuntimeDeviceId.toLong(),
            onnxRuntimeTileSize = settings.onnxRuntimeTileSize.toLong(),
            readerType = settings.readerType.name,
            stretchToFit = settings.stretchToFit,
            pagedScaleType = settings.pagedScaleType.name,
            pagedReadingDirection = settings.pagedReadingDirection.name,
            pagedPageLayout = settings.pagedPageLayout.name,
            continuousReadingDirection = settings.continuousReadingDirection.name,
            continuousPadding = settings.continuousPadding.toDouble(),
            continuousPageSpacing = settings.continuousPageSpacing.toLong(),
            komfEnabled = settings.komfEnabled,
            komfMode = settings.komfMode.name,
            komfRemoteUrl = settings.komfRemoteUrl
        )
    }
}