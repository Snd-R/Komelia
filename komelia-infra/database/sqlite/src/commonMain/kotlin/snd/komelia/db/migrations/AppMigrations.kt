package snd.komelia.db.migrations

import io.github.snd_r.komelia.db.sqlite.sqlite.generated.resources.Res
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class AppMigrations : MigrationResourcesProvider() {

    private val migrations = listOf(
        "V1__initial_migration.sql",
        "V2__komga_webui_reader_settings.sql",
        "V3__exposed_migration.sql",
        "V4__settings_reorganisation.sql",
        "V5__color_correction.sql",
        "V6__eink_screen_flash.sql",
        "V7__reader_sampling_settings.sql",
        "V8__thumbnail_previews.sql",
        "V9__volume_keys_navigation.sql",
        "V10__komf_settings.sql",
        "V11__home_filters.sql",
        "V12__offline_mode.sql",
    )

    override suspend fun getMigration(name: String): ByteArray? {
        return try {
            Res.readBytes("files/migrations/app/$name")
        } catch (e: Exception) {
            currentCoroutineContext().ensureActive()
            null
        }
    }

    override suspend fun getMigrations(): Map<String, ByteArray> {
        return migrations.associateWith { Res.readBytes("files/migrations/app/$it") }
    }
}