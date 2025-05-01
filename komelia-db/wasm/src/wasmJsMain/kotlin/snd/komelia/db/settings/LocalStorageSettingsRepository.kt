package snd.komelia.db.settings

import io.github.snd_r.komelia.image.UpsamplingMode
import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json
import org.w3c.dom.set
import snd.komelia.db.AppSettings
import snd.komelia.db.EpubReaderSettings
import snd.komelia.db.ImageReaderSettings
import snd.komelia.db.KomfSettings

const val appSettingsKey = "appSettings"
const val imageReaderKey = "imageReader"
const val epubReaderKey = "epubReader"
const val komfSettingsKey = "komfSettings"

class LocalStorageSettingsRepository {
    val json = Json {
        ignoreUnknownKeys = true
    }

    fun getSettings(): AppSettings {
        return localStorage.getItem(appSettingsKey)
            ?.let { json.decodeFromString<AppSettings>(it) }
            ?: AppSettings()
    }

    fun saveAppSettings(settings: AppSettings) {
        localStorage[appSettingsKey] = json.encodeToString(settings)
    }

    fun getImageReaderSettings(): ImageReaderSettings {
        return localStorage.getItem(imageReaderKey)
            ?.let { json.decodeFromString<ImageReaderSettings>(it) }
            ?: ImageReaderSettings(upsamplingMode = UpsamplingMode.CATMULL_ROM)
    }

    fun saveImageReaderSettings(settings: ImageReaderSettings) {
        localStorage[imageReaderKey] = json.encodeToString(settings)
    }

    fun getEpubReaderSettings(): EpubReaderSettings {
        return localStorage.getItem(epubReaderKey)
            ?.let { json.decodeFromString<EpubReaderSettings>(it) }
            ?: EpubReaderSettings()
    }

    fun saveEpubReaderSettings(settings: EpubReaderSettings) {
        localStorage[epubReaderKey] = json.encodeToString(settings)
    }

    fun getKomfSettings(): KomfSettings {
        return localStorage.getItem(komfSettingsKey)
            ?.let { json.decodeFromString<KomfSettings>(it) }
            ?: KomfSettings()
    }

    fun saveKomfSettings(settings: KomfSettings) {
        localStorage[komfSettingsKey] = json.encodeToString(settings)
    }
}