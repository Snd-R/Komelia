package snd.komelia.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AppSettingsTable : Table("AppSettings") {
    val version = integer("version")
    val username = text("username")
    val serverUrl = text("serverUrl")
    val cardWidth = integer("card_width")
    val seriesPageLoadSize = integer("series_page_load_size")

    val bookPageLoadSize = integer("book_page_load_size")
    val bookListLayout = text("book_list_layout")
    val appTheme = text("app_theme")

    val checkForUpdatesOnStartup = bool("check_for_updates_on_startup")
    val updateLastCheckedTimestamp = timestamp("update_last_checked_timestamp").nullable()
    val updateLastCheckedReleaseVersion = text("update_last_checked_release_version").nullable()
    val updateDismissedVersion = text("update_dismissed_version").nullable()

    val upscaleOption = text("upscale_option")
    val downscaleOption = text("downscale_option")
    val onnxModelsPath = text("onnx_models_path")
    val onnxRuntimeDeviceId = integer("onnxRuntime_device_id")
    val onnxRuntimeTileSize = integer("onnxRuntime_tile_size")

    val readerType = text("reader_type")
    val stretchToFit = bool("stretch_to_fit")

    val pagedScaleType = text("paged_scale_type")
    val pagedReadingDirection = text("paged_reading_direction")
    val pagedPageLayout = text("paged_page_layout")

    val continuousReadingDirection = text("continuous_reading_direction")
    val continuousPadding = float("continuous_padding")
    val continuousPageSpacing = integer("continuous_page_spacing")
    val cropBorders = bool("crop_borders")

    val komfEnabled = bool("komf_enabled")
    val komfMode = text("komf_mode")
    val komfRemoteUrl = text("komf_remote_url")

    override val primaryKey = PrimaryKey(version)
}