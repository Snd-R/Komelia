package snd.komelia.db.settings

import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.set
import snd.komelia.db.AppSettings
import snd.komelia.db.EpubReaderSettings
import snd.komelia.db.ImageReaderSettings

const val appSettingsKey = "appSettings"
const val imageReaderKey = "imageReader"
const val epubReaderKey = "epubReader"

class LocalStorageSettingsRepository {

    fun getSettings(): AppSettings {
        return localStorage.getItem(appSettingsKey)
            ?.let { Json.decodeFromString<AppSettings>(it) }
            ?: AppSettings(upscaleOption = "Default", downscaleOption = "Default")
    }

    fun saveAppSettings(settings: AppSettings) {
        localStorage[appSettingsKey] = Json.encodeToString(settings)
    }

    fun getImageReaderSettings(): ImageReaderSettings {
        return localStorage.getItem(imageReaderKey)
            ?.let { Json.decodeFromString<ImageReaderSettings>(it) }
            ?: ImageReaderSettings()
    }

    fun saveImageReaderSettings(settings: ImageReaderSettings) {
        localStorage[imageReaderKey] = Json.encodeToString(settings)
    }

    fun getEpubReaderSettings(): EpubReaderSettings {
        return localStorage.getItem(epubReaderKey)
            ?.let { Json.decodeFromString<EpubReaderSettings>(it) }
            ?: EpubReaderSettings()
    }

    fun saveEpubReaderSettings(settings: EpubReaderSettings) {
        localStorage[epubReaderKey] = Json.encodeToString(settings)
    }
}