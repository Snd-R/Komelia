package snd.komelia.db.settings

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.ImageReaderSettings
import snd.komelia.db.defaultBookId
import snd.komelia.db.tables.ImageReaderSettingsTable
import snd.komelia.image.ReduceKernel
import snd.komelia.image.UpsamplingMode
import snd.komelia.image.UpscaleMode
import snd.komelia.settings.model.ContinuousReadingDirection
import snd.komelia.settings.model.LayoutScaleType
import snd.komelia.settings.model.PageDisplayLayout
import snd.komelia.settings.model.PagedReadingDirection
import snd.komelia.settings.model.ReaderFlashColor
import snd.komelia.settings.model.ReaderType

class ExposedImageReaderSettingsRepository(database: Database) : ExposedRepository(database) {

    suspend fun get(): ImageReaderSettings? {
        return transaction {
            ImageReaderSettingsTable.selectAll()
                .where { ImageReaderSettingsTable.bookId.eq(defaultBookId) }
                .firstOrNull()
                ?.let {

                    ImageReaderSettings(
                        readerType = ReaderType.valueOf(it[ImageReaderSettingsTable.readerType]),
                        stretchToFit = it[ImageReaderSettingsTable.stretchToFit],
                        pagedScaleType = LayoutScaleType.valueOf(it[ImageReaderSettingsTable.pagedScaleType]),
                        pagedReadingDirection = PagedReadingDirection.valueOf(it[ImageReaderSettingsTable.pagedReadingDirection]),
                        pagedPageLayout = PageDisplayLayout.valueOf(it[ImageReaderSettingsTable.pagedPageLayout]),
                        continuousReadingDirection = ContinuousReadingDirection.valueOf(it[ImageReaderSettingsTable.continuousReadingDirection]),
                        continuousPadding = it[ImageReaderSettingsTable.continuousPadding],
                        continuousPageSpacing = it[ImageReaderSettingsTable.continuousPageSpacing],
                        cropBorders = it[ImageReaderSettingsTable.cropBorders],
                        flashOnPageChange = it[ImageReaderSettingsTable.flashOnPageChange],
                        flashDuration = it[ImageReaderSettingsTable.flashDuration],
                        flashEveryNPages = it[ImageReaderSettingsTable.flashEveryNPages],
                        flashWith = ReaderFlashColor.valueOf(it[ImageReaderSettingsTable.flashWith]),
                        downsamplingKernel = ReduceKernel.valueOf(it[ImageReaderSettingsTable.downsamplingKernel]),
                        linearLightDownsampling = it[ImageReaderSettingsTable.linearLightDownsampling],
                        upsamplingMode = UpsamplingMode.valueOf(it[ImageReaderSettingsTable.upsamplingMode]),
                        loadThumbnailPreviews = it[ImageReaderSettingsTable.loadThumbnailPreviews],
                        volumeKeysNavigation = it[ImageReaderSettingsTable.volumeKeysNavigation],
                        ortUpscalerMode = UpscaleMode.valueOf(it[ImageReaderSettingsTable.ortUpscalerMode]),
                        ortUpscalerUserModelPath = it[ImageReaderSettingsTable.ortUpscalerUserModelPath]
                            ?.let { PlatformFile(it) },
                        ortUpscalerDeviceId = it[ImageReaderSettingsTable.ortDeviceId],
                        ortUpscalerTileSize = it[ImageReaderSettingsTable.ortUpscalerTileSize],
                    )
                }
        }
    }

    suspend fun save(settings: ImageReaderSettings) {
        transaction {
            ImageReaderSettingsTable.upsert {
                it[bookId] = defaultBookId
                it[readerType] = settings.readerType.name
                it[stretchToFit] = settings.stretchToFit
                it[pagedScaleType] = settings.pagedScaleType.name
                it[pagedReadingDirection] = settings.pagedReadingDirection.name
                it[pagedPageLayout] = settings.pagedPageLayout.name
                it[continuousReadingDirection] = settings.continuousReadingDirection.name
                it[continuousPadding] = settings.continuousPadding
                it[continuousPageSpacing] = settings.continuousPageSpacing
                it[cropBorders] = settings.cropBorders
                it[flashOnPageChange] = settings.flashOnPageChange
                it[flashDuration] = settings.flashDuration
                it[flashEveryNPages] = settings.flashEveryNPages
                it[flashWith] = settings.flashWith.name
                it[downsamplingKernel] = settings.downsamplingKernel.name
                it[linearLightDownsampling] = settings.linearLightDownsampling
                it[loadThumbnailPreviews] = settings.loadThumbnailPreviews
                it[volumeKeysNavigation] = settings.volumeKeysNavigation
                it[upsamplingMode] = settings.upsamplingMode.name
                it[ortUpscalerMode] = settings.ortUpscalerMode.name
                it[ortUpscalerUserModelPath] = settings.ortUpscalerUserModelPath?.path
                it[ortDeviceId] = settings.ortUpscalerDeviceId
                it[ortUpscalerTileSize] = settings.ortUpscalerTileSize
            }
        }
    }
}