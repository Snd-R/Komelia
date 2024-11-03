package snd.komelia.db.settings

import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

class ExposedSettingsRepository(
    private val database: Database
) {

    fun get(): AppSettings? {
        return transaction(database) {
            AppSettingsTable.selectAll()
                .firstOrNull()
                ?.toAppSettings()
        }
    }

    fun save(settings: AppSettings) {
        transaction(database) {
            AppSettingsTable.upsert {
                it[version] = 1
                it[username] = settings.username
                it[serverUrl] = settings.serverUrl
                it[cardWidth] = settings.cardWidth

                it[seriesPageLoadSize] = settings.seriesPageLoadSize
                it[bookPageLoadSize] = settings.bookPageLoadSize
                it[bookListLayout] = settings.bookListLayout.name
                it[appTheme] = settings.appTheme.name

                it[checkForUpdatesOnStartup] = settings.checkForUpdatesOnStartup
                it[updateLastCheckedTimestamp] = settings.updateLastCheckedTimestamp
                it[updateLastCheckedReleaseVersion] = settings.updateLastCheckedReleaseVersion?.toString()
                it[updateDismissedVersion] = settings.updateDismissedVersion?.toString()
                it[upscaleOption] = settings.upscaleOption
                it[downscaleOption] = settings.downscaleOption
                it[onnxModelsPath] = settings.onnxModelsPath
                it[onnxRuntimeDeviceId] = settings.onnxRuntimeDeviceId
                it[onnxRuntimeTileSize] = settings.onnxRuntimeTileSize
                it[readerType] = settings.readerType.name
                it[stretchToFit] = settings.stretchToFit
                it[pagedScaleType] = settings.pagedScaleType.name
                it[pagedReadingDirection] = settings.pagedReadingDirection.name
                it[pagedPageLayout] = settings.pagedPageLayout.name
                it[continuousReadingDirection] = settings.continuousReadingDirection.name
                it[continuousPadding] = settings.continuousPadding
                it[continuousPageSpacing] = settings.continuousPageSpacing
                it[cropBorders] = settings.cropBorders
                it[komfEnabled] = settings.komfEnabled
                it[komfMode] = settings.komfMode.name
                it[komfRemoteUrl] = settings.komfRemoteUrl
                it[komgaWebuiEpubReaderSettings] = Json.encodeToString(settings.komgaWebuiEpubReader)
            }
        }
    }

    private fun ResultRow.toAppSettings(): AppSettings {
        return AppSettings(
            username = get(AppSettingsTable.username),
            serverUrl = get(AppSettingsTable.serverUrl),
            cardWidth = get(AppSettingsTable.cardWidth),
            seriesPageLoadSize = get(AppSettingsTable.seriesPageLoadSize),
            bookPageLoadSize = get(AppSettingsTable.bookPageLoadSize),
            bookListLayout = BooksLayout.valueOf(get(AppSettingsTable.bookListLayout)),
            appTheme = AppTheme.valueOf(get(AppSettingsTable.appTheme)),
            checkForUpdatesOnStartup = get(AppSettingsTable.checkForUpdatesOnStartup),
            updateLastCheckedTimestamp = get(AppSettingsTable.updateLastCheckedTimestamp),
            updateLastCheckedReleaseVersion = get(AppSettingsTable.updateLastCheckedReleaseVersion)
                ?.let { AppVersion.fromString(it) },
            updateDismissedVersion = get(AppSettingsTable.updateDismissedVersion)
                ?.let { AppVersion.fromString(it) },
            upscaleOption = get(AppSettingsTable.upscaleOption),
            downscaleOption = get(AppSettingsTable.downscaleOption),
            onnxModelsPath = get(AppSettingsTable.onnxModelsPath),
            onnxRuntimeDeviceId = get(AppSettingsTable.onnxRuntimeDeviceId),
            onnxRuntimeTileSize = get(AppSettingsTable.onnxRuntimeTileSize),
            readerType = ReaderType.valueOf(get(AppSettingsTable.readerType)),
            stretchToFit = get(AppSettingsTable.stretchToFit),
            pagedScaleType = LayoutScaleType.valueOf(get(AppSettingsTable.pagedScaleType)),
            pagedReadingDirection = PagedReaderState.ReadingDirection.valueOf(get(AppSettingsTable.pagedReadingDirection)),
            pagedPageLayout = PagedReaderState.PageDisplayLayout.valueOf(get(AppSettingsTable.pagedPageLayout)),
            continuousReadingDirection = ContinuousReaderState.ReadingDirection.valueOf(get(AppSettingsTable.continuousReadingDirection)),
            continuousPadding = get(AppSettingsTable.continuousPadding),
            continuousPageSpacing = get(AppSettingsTable.continuousPageSpacing),
            cropBorders = get(AppSettingsTable.cropBorders),
            komfEnabled = get(AppSettingsTable.komfEnabled),
            komfMode = KomfMode.valueOf(get(AppSettingsTable.komfMode)),
            komfRemoteUrl = get(AppSettingsTable.komfRemoteUrl),
            komgaWebuiEpubReader = get(AppSettingsTable.komgaWebuiEpubReaderSettings)
                ?.let { Json.decodeFromString<JsonObject>(it) }
                ?: buildJsonObject { }
        )
    }

}