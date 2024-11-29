package io.github.snd_r.komelia.settings

import io.github.snd_r.komelia.ui.reader.epub.TtsuReaderSettings
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

interface EpubReaderSettingsRepository {
    fun getReaderType(): Flow<EpubReaderType>
    suspend fun putReaderType(type: EpubReaderType)

    suspend fun getKomgaReaderSettings(): JsonObject
    suspend fun putKomgaReaderSettings(settings: JsonObject)

    suspend fun getTtsuReaderSettings(): TtsuReaderSettings
    suspend fun putTtsuReaderSettings(settings: TtsuReaderSettings)
}