package snd.komelia.db.settings

import io.github.snd_r.komelia.platform.DownscaleOption
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject

class SharedActorSettingsRepository(
    private val actor: SettingsActor,
) : CommonSettingsRepository {

    override fun getServerUrl(): Flow<String> {
        return actor.state.map { it.serverUrl }.distinctUntilChanged()
    }

    override suspend fun putServerUrl(url: String) {
        actor.transform { it.copy(serverUrl = url) }
    }

    override fun getCardWidth(): Flow<Int> {
        return actor.state.map { it.cardWidth }.distinctUntilChanged()
    }

    override suspend fun putCardWidth(cardWidth: Int) {
        actor.transform { it.copy(cardWidth = cardWidth) }
    }

    override fun getCurrentUser(): Flow<String> {
        return actor.state.map { it.username }.distinctUntilChanged()
    }

    override suspend fun putCurrentUser(username: String) {
        actor.transform { it.copy(username = username) }
    }

    override fun getDecoderSettings(): Flow<PlatformDecoderSettings> {
        return actor.state.map { settings ->
            PlatformDecoderSettings(
                upscaleOption = UpscaleOption(settings.upscaleOption),
                downscaleOption = DownscaleOption(settings.downscaleOption)
            )
        }.distinctUntilChanged()
    }

    override suspend fun putDecoderSettings(decoder: PlatformDecoderSettings) {
        actor.transform { settings ->
            settings.copy(
                upscaleOption = decoder.upscaleOption.value,
                downscaleOption = decoder.downscaleOption.value
            )
        }
    }

    override fun getSeriesPageLoadSize(): Flow<Int> {
        return actor.state.map { it.seriesPageLoadSize }.distinctUntilChanged()
    }

    override suspend fun putSeriesPageLoadSize(size: Int) {
        actor.transform { it.copy(seriesPageLoadSize = size) }
    }

    override fun getBookPageLoadSize(): Flow<Int> {
        return actor.state.map { it.bookPageLoadSize }.distinctUntilChanged()
    }

    override suspend fun putBookPageLoadSize(size: Int) {
        actor.transform { it.copy(bookPageLoadSize = size) }
    }

    override fun getBookListLayout(): Flow<BooksLayout> {
        return actor.state.map { it.bookListLayout }.distinctUntilChanged()
    }

    override suspend fun putBookListLayout(layout: BooksLayout) {
        actor.transform { it.copy(bookListLayout = layout) }
    }

    override fun getCheckForUpdatesOnStartup(): Flow<Boolean> {
        return actor.state.map { it.checkForUpdatesOnStartup }.distinctUntilChanged()
    }

    override suspend fun putCheckForUpdatesOnStartup(check: Boolean) {
        actor.transform { it.copy(checkForUpdatesOnStartup = check) }
    }

    override fun getLastUpdateCheckTimestamp(): Flow<Instant?> {
        return actor.state.map { it.updateLastCheckedTimestamp }.distinctUntilChanged()
    }

    override suspend fun putLastUpdateCheckTimestamp(timestamp: Instant) {
        actor.transform { it.copy(updateLastCheckedTimestamp = timestamp) }
    }

    override fun getLastCheckedReleaseVersion(): Flow<AppVersion?> {
        return actor.state.map { it.updateLastCheckedReleaseVersion }.distinctUntilChanged()
    }

    override suspend fun putLastCheckedReleaseVersion(version: AppVersion) {
        actor.transform { it.copy(updateLastCheckedReleaseVersion = version) }
    }

    override fun getDismissedVersion(): Flow<AppVersion?> {
        return actor.state.map { it.updateDismissedVersion }.distinctUntilChanged()
    }

    override suspend fun putDismissedVersion(version: AppVersion) {
        actor.transform { it.copy(updateDismissedVersion = version) }
    }

    override fun getAppTheme(): Flow<AppTheme> {
        return actor.state.map { it.appTheme }.distinctUntilChanged()
    }

    override suspend fun putAppTheme(theme: AppTheme) {
        actor.transform { it.copy(appTheme = theme) }
    }

    override fun getKomfEnabled(): Flow<Boolean> {
        return actor.state.map { it.komfEnabled }.distinctUntilChanged()
    }

    override suspend fun putKomfEnabled(enabled: Boolean) {
        actor.transform { it.copy(komfEnabled = enabled) }
    }

    override fun getKomfMode(): Flow<KomfMode> {
        return actor.state.map { it.komfMode }.distinctUntilChanged()
    }

    override suspend fun putKomfMode(mode: KomfMode) {
        actor.transform { it.copy(komfMode = mode) }
    }

    override fun getKomfUrl(): Flow<String> {
        return actor.state.map { it.komfRemoteUrl }.distinctUntilChanged()
    }

    override suspend fun putKomfUrl(url: String) {
        actor.transform { it.copy(komfRemoteUrl = url) }
    }

    override fun getOnnxModelsPath(): Flow<String> {
        return actor.state.map { it.onnxModelsPath }.distinctUntilChanged()
    }

    override suspend fun putOnnxModelsPath(path: String) {
        actor.transform { it.copy(onnxModelsPath = path) }
    }

    override fun getOnnxRuntimeDeviceId(): Flow<Int> {
        return actor.state.map { it.onnxRuntimeDeviceId }.distinctUntilChanged()
    }

    override suspend fun putOnnxRuntimeDeviceId(deviceId: Int) {
        actor.transform { it.copy(onnxRuntimeDeviceId = deviceId) }
    }

    override fun getOnnxRuntimeTileSize(): Flow<Int> {
        return actor.state.map { it.onnxRuntimeTileSize }.distinctUntilChanged()
    }

    override suspend fun putOnnxRuntimeTileSize(tileSize: Int) {
        actor.transform { it.copy(onnxRuntimeTileSize = tileSize) }
    }
}