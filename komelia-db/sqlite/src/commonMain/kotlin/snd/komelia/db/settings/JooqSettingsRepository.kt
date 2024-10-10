package snd.komelia.db.settings

import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.datetime.Instant
import org.jooq.DSLContext
import snd.komelia.db.jooq.tables.records.AppsettingsRecord
import snd.komelia.db.jooq.tables.references.APPSETTINGS

class JooqSettingsRepository(
    private val dsl: DSLContext,
) {

    fun get(): AppSettings? {
        return dsl.selectFrom(APPSETTINGS).fetchOne()?.fromRecord()
    }

    fun save(settings: AppSettings) {
        val record = settings.toRecord()
        dsl.insertInto(APPSETTINGS, *APPSETTINGS.fields())
            .values(record)
            .onConflict()
            .doUpdate()
            .set(record)
            .execute()
    }

    private fun AppsettingsRecord.fromRecord(): AppSettings {
        return AppSettings(
            username = username,
            serverUrl = serverurl,
            cardWidth = cardWidth,
            seriesPageLoadSize = seriesPageLoadSize,
            bookPageLoadSize = bookPageLoadSize,
            bookListLayout = BooksLayout.valueOf(bookListLayout),
            appTheme = AppTheme.valueOf(appTheme),
            checkForUpdatesOnStartup = checkForUpdatesOnStartup,
            updateLastCheckedTimestamp = updateLastCheckedTimestamp?.let { Instant.fromEpochMilliseconds(it) },
            updateLastCheckedReleaseVersion = updateLastCheckedReleaseVersion?.let { AppVersion.fromString(it) },
            updateDismissedVersion = updateDismissedVersion?.let { AppVersion.fromString(it) },
            upscaleOption = upscaleOption,
            downscaleOption = downscaleOption,
            onnxModelsPath = onnxModelsPath,
            onnxRuntimeDeviceId = onnxruntimeDeviceDd,
            onnxRuntimeTileSize = onnxruntimeTileSize,
            readerType = ReaderType.valueOf(readerType),
            stretchToFit = stretchToFit,
            pagedScaleType = LayoutScaleType.valueOf(pagedScaleType),
            pagedReadingDirection = PagedReaderState.ReadingDirection.valueOf(pagedReadingDirection),
            pagedPageLayout = PagedReaderState.PageDisplayLayout.valueOf(pagedPageLayout),
            continuousReadingDirection = ContinuousReaderState.ReadingDirection.valueOf(continuousReadingDirection),
            continuousPadding = continuousPadding,
            continuousPageSpacing = continuousPageSpacing,
            cropBorders = cropBorders,
            komfEnabled = komfEnabled,
            komfMode = KomfMode.valueOf(komfMode),
            komfRemoteUrl = komfRemoteUrl
        )
    }

    private fun AppSettings.toRecord(): AppsettingsRecord {
        return AppsettingsRecord(
            version = 1,
            username = username,
            serverurl = serverUrl,
            cardWidth = cardWidth,
            seriesPageLoadSize = seriesPageLoadSize,
            bookPageLoadSize = bookPageLoadSize,
            bookListLayout = bookListLayout.name,
            appTheme = appTheme.name,
            checkForUpdatesOnStartup = checkForUpdatesOnStartup,
            updateLastCheckedTimestamp = updateLastCheckedTimestamp?.toEpochMilliseconds(),
            updateLastCheckedReleaseVersion = updateLastCheckedReleaseVersion?.toString(),
            updateDismissedVersion = updateDismissedVersion?.toString(),
            upscaleOption = upscaleOption,
            downscaleOption = downscaleOption,
            onnxModelsPath = onnxModelsPath,
            onnxruntimeDeviceDd = onnxRuntimeDeviceId,
            onnxruntimeTileSize = onnxRuntimeTileSize,
            readerType = readerType.name,
            stretchToFit = stretchToFit,
            pagedScaleType = pagedScaleType.name,
            pagedReadingDirection = pagedReadingDirection.name,
            pagedPageLayout = pagedPageLayout.name,
            continuousReadingDirection = continuousReadingDirection.name,
            continuousPadding = continuousPadding,
            continuousPageSpacing = continuousPageSpacing,
            cropBorders = cropBorders,
            komfEnabled = komfEnabled,
            komfMode = komfMode.name,
            komfRemoteUrl = komfRemoteUrl
        )
    }
}