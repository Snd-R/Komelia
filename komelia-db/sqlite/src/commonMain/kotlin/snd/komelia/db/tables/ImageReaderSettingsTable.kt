package snd.komelia.db.tables

import org.jetbrains.exposed.sql.Table
import snd.komelia.db.tables.AppSettingsTable.version

object ImageReaderSettingsTable: Table("ImageReaderSettings") {
    val bookId = text("book_id")

    val readerType = text("reader_type")
    val stretchToFit = bool("stretch_to_fit")

    val pagedScaleType = text("paged_scale_type")
    val pagedReadingDirection = text("paged_reading_direction")
    val pagedPageLayout = text("paged_page_layout")

    val continuousReadingDirection = text("continuous_reading_direction")
    val continuousPadding = float("continuous_padding")
    val continuousPageSpacing = integer("continuous_page_spacing")
    val cropBorders = bool("crop_borders")

    override val primaryKey = PrimaryKey(version)
}