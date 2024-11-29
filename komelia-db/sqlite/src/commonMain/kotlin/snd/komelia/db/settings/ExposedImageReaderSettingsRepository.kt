package snd.komelia.db.settings

import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.continuous.ContinuousReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState
import io.github.snd_r.komelia.ui.reader.paged.PagedReaderState.LayoutScaleType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.ImageReaderSettings
import snd.komelia.db.defaultBookId
import snd.komelia.db.tables.ImageReaderSettingsTable

class ExposedImageReaderSettingsRepository(database: Database) : ExposedRepository(database) {

    suspend fun get(): ImageReaderSettings? {
        return transactionOnDefaultDispatcher {
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
                    )
                }
        }
    }

    suspend fun save(settings: ImageReaderSettings) {
        transactionOnDefaultDispatcher {
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
            }
        }
    }
}