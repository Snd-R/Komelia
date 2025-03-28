package snd.komelia.db.settings

import io.github.snd_r.komelia.image.UpsamplingMode
import io.github.snd_r.komelia.ui.reader.image.ReaderFlashColor
import io.github.snd_r.komelia.ui.reader.image.ReaderType
import io.github.snd_r.komelia.ui.reader.image.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.image.paged.PagedReaderState.LayoutScaleType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.ImageReaderSettings
import snd.komelia.db.defaultBookId
import snd.komelia.db.tables.ImageReaderSettingsTable
import snd.komelia.image.OnnxRuntimeUpscaleMode
import snd.komelia.image.ReduceKernel

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
                        pagedReadingDirection = PagedReaderState.ReadingDirection.valueOf(it[ImageReaderSettingsTable.pagedReadingDirection]),
                        pagedPageLayout = PagedReaderState.PageDisplayLayout.valueOf(it[ImageReaderSettingsTable.pagedPageLayout]),
                        continuousReadingDirection = ContinuousReaderState.ReadingDirection.valueOf(it[ImageReaderSettingsTable.continuousReadingDirection]),
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
                        onnxRuntimeMode = OnnxRuntimeUpscaleMode.valueOf(it[ImageReaderSettingsTable.onnxRuntimeMode]),
                        onnxRuntimeModelPath = it[ImageReaderSettingsTable.onnxRuntimeModelPath],
                        onnxRuntimeDeviceId = it[ImageReaderSettingsTable.onnxRuntimeDeviceId],
                        onnxRuntimeTileSize = it[ImageReaderSettingsTable.onnxRuntimeTileSize],
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
                it[onnxRuntimeMode] = settings.onnxRuntimeMode.name
                it[onnxRuntimeModelPath] = settings.onnxRuntimeModelPath
                it[onnxRuntimeDeviceId] = settings.onnxRuntimeDeviceId
                it[onnxRuntimeTileSize] = settings.onnxRuntimeTileSize
            }
        }
    }
}