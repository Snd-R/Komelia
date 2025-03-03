package snd.komelia.db.tables

import org.jetbrains.exposed.sql.Table

object ImageReaderSettingsTable : Table("ImageReaderSettings") {
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

    val loadThumbnailPreviews = bool("load_thumbnail_previews")

    val flashOnPageChange = bool("flash_on_page_change")
    val flashDuration = long("flash_duration")
    val flashEveryNPages = integer("flash_every_n_pages")
    val flashWith = text("flash_with")

    val downsamplingKernel = text("downsampling_kernel")
    val linearLightDownsampling = bool("linear_light_downsampling")
    val upsamplingMode = text("upsampling_mode")
    val onnxRuntimeMode = text("onnx_runtime_mode")
    val onnxRuntimeDeviceId = integer("onnx_runtime_device_id")
    val onnxRuntimeTileSize = integer("onnx_runtime_tile_size")
    val onnxRuntimeModelPath = text("onnx_runtime_model_path").nullable()

    override val primaryKey = PrimaryKey(bookId)
}