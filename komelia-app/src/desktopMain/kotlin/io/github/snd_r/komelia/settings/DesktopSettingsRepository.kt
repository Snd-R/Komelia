package io.github.snd_r.komelia.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.DownscaleOption
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.series.BooksLayout
import io.github.snd_r.komelia.ui.settings.komf.KomfMode
import io.github.snd_r.komelia.updates.AppVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class DesktopSettingsRepository(private val actor: FileSystemSettingsActor) : SettingsRepository {

    override fun getServerUrl(): Flow<String> {
        return actor.getState().map { it.server.url }.distinctUntilChanged()
    }

    override suspend fun putServerUrl(url: String) {
        actor.transform { settings -> settings.copy(server = settings.server.copy(url = url)) }
    }

    override fun getCardWidth(): Flow<Dp> {
        return actor.getState().map { it.appearance.cardWidth.dp }.distinctUntilChanged()
    }

    override suspend fun putCardWidth(cardWidth: Dp) {
        actor.transform { settings -> settings.copy(appearance = settings.appearance.copy(cardWidth = cardWidth.value.toInt())) }
    }

    override fun getCurrentUser(): Flow<String> {
        return actor.getState().map { it.user.username }.distinctUntilChanged()
    }

    override suspend fun putCurrentUser(username: String) {
        actor.transform { settings -> settings.copy(user = settings.user.copy(username = username)) }
    }

    override fun getDecoderSettings(): Flow<PlatformDecoderSettings> {
        return actor.getState().map {
            val decoderSettings = it.decoder
            PlatformDecoderSettings(
                platformType = decoderSettings.decoder,
                upscaleOption = UpscaleOption(decoderSettings.upscaleOption),
                downscaleOption = DownscaleOption(decoderSettings.downscaleOption)
            )
        }.distinctUntilChanged()
    }

    override suspend fun putDecoderSettings(decoder: PlatformDecoderSettings) {
        actor.transform { settings ->
            settings.copy(
                decoder = settings.decoder.copy(
                    decoder = decoder.platformType,
                    upscaleOption = decoder.upscaleOption.value,
                    downscaleOption = decoder.downscaleOption.value
                )
            )
        }
    }

    override fun getSeriesPageLoadSize(): Flow<Int> {
        return actor.getState().map { it.appearance.seriesPageLoadSize }.distinctUntilChanged()
    }

    override suspend fun putSeriesPageLoadSize(size: Int) {
        actor.transform { settings -> settings.copy(appearance = settings.appearance.copy(seriesPageLoadSize = size)) }
    }

    override fun getBookPageLoadSize(): Flow<Int> {
        return actor.getState().map { it.appearance.bookPageLoadSize }.distinctUntilChanged()
    }

    override suspend fun putBookPageLoadSize(size: Int) {
        actor.transform { settings -> settings.copy(appearance = settings.appearance.copy(bookPageLoadSize = size)) }
    }

    override fun getBookListLayout(): Flow<BooksLayout> {
        return actor.getState().map { it.appearance.bookListLayout }.distinctUntilChanged()
    }

    override suspend fun putBookListLayout(layout: BooksLayout) {
        actor.transform { settings -> settings.copy(appearance = settings.appearance.copy(bookListLayout = layout)) }
    }

    override fun getCheckForUpdatesOnStartup(): Flow<Boolean> {
        return actor.getState().map { it.updates.checkForUpdatesOnStartup }.distinctUntilChanged()
    }

    override suspend fun putCheckForUpdatesOnStartup(check: Boolean) {
        actor.transform { settings -> settings.copy(updates = settings.updates.copy(checkForUpdatesOnStartup = check)) }
    }

    override fun getLastUpdateCheckTimestamp(): Flow<Instant?> {
        return actor.getState().map { it.updates.lastUpdateCheckTimestamp }.distinctUntilChanged()
    }

    override suspend fun putLastUpdateCheckTimestamp(timestamp: Instant) {
        actor.transform { settings -> settings.copy(updates = settings.updates.copy(lastUpdateCheckTimestamp = timestamp)) }
    }

    override fun getLastCheckedReleaseVersion(): Flow<AppVersion?> {
        return actor.getState().map { it.updates.lastCheckedReleaseVersion }.distinctUntilChanged()
    }

    override suspend fun putLastCheckedReleaseVersion(version: AppVersion) {
        actor.transform { settings -> settings.copy(updates = settings.updates.copy(lastCheckedReleaseVersion = version)) }
    }

    override fun getDismissedVersion(): Flow<AppVersion?> {
        return actor.getState().map { it.updates.dismissedVersion }.distinctUntilChanged()
    }

    override suspend fun putDismissedVersion(version: AppVersion) {
        actor.transform { settings -> settings.copy(updates = settings.updates.copy(dismissedVersion = version)) }
    }

    override fun getAppTheme(): Flow<AppTheme> {
        return actor.getState().map { it.appearance.appTheme }.distinctUntilChanged()
    }

    override suspend fun putAppTheme(theme: AppTheme) {
        actor.transform { settings -> settings.copy(appearance = settings.appearance.copy(appTheme = theme)) }
    }

    override fun getKomfEnabled(): Flow<Boolean> {
        return actor.getState().map { it.komf.enabled }.distinctUntilChanged()
    }

    override suspend fun putKomfEnabled(enabled: Boolean) {
        actor.transform { settings -> settings.copy(komf = settings.komf.copy(enabled = enabled)) }
    }

    override fun getKomfMode(): Flow<KomfMode> {
        return actor.getState().map { it.komf.mode }.distinctUntilChanged()
    }

    override suspend fun putKomfMode(mode: KomfMode) {
        actor.transform { settings -> settings.copy(komf = settings.komf.copy(mode = mode)) }
    }

    override fun getKomfUrl(): Flow<String> {
        return actor.getState().map { it.komf.remoteUrl }.distinctUntilChanged()
    }

    override suspend fun putKomfUrl(url: String) {
        actor.transform { settings -> settings.copy(komf = settings.komf.copy(remoteUrl = url)) }
    }

    fun getOnnxModelsPath(): Flow<String> {
        return actor.getState().map { it.decoder.onnxModelsPath }.distinctUntilChanged()
    }

    suspend fun putOnnxModelsPath(path: String) {
        actor.transform { settings -> settings.copy(decoder = settings.decoder.copy(onnxModelsPath = path)) }
    }

    fun getOnnxRuntimeDeviceId(): Flow<Int> {
        return actor.getState().map { it.decoder.onnxRutnimeDeviceId }.distinctUntilChanged()
    }

    suspend fun putOnnxRuntimeDeviceId(deviceId: Int) {
        actor.transform { settings -> settings.copy(decoder = settings.decoder.copy(onnxRutnimeDeviceId = deviceId)) }
    }

    fun getOnnxRuntimeTileSize(): Flow<Int> {
        return actor.getState().map { it.decoder.onnxRuntimeTileSize }.distinctUntilChanged()
    }

    suspend fun putOnnxRuntimeTileSize(tileSize: Int) {
        actor.transform { settings -> settings.copy(decoder = settings.decoder.copy(onnxRuntimeTileSize = tileSize)) }
    }
}