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

    override val primaryKey = PrimaryKey(version)
}