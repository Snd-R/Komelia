package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.ui.reader.epub.TtsuReaderSettings
import kotlinx.serialization.json.JsonObject

interface EpubReaderSettingsRepository {
    suspend fun getKomgaReaderSettings(): JsonObject
    suspend fun putKomgaReaderSettings(settings: JsonObject)

    suspend fun getTtsuReaderSettings(): TtsuReaderSettings
    suspend fun putTtsuReaderSettings(settings: TtsuReaderSettings)
}