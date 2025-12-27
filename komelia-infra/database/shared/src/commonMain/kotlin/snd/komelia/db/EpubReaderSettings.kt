package snd.komelia.db

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import snd.komelia.settings.model.EpubReaderType
import snd.komelia.settings.model.TtsuReaderSettings

@Serializable
data class EpubReaderSettings(
    val readerType: EpubReaderType = EpubReaderType.TTSU_EPUB,
    val komgaReaderSettings: JsonObject = buildJsonObject { },
    val ttsuReaderSettings: TtsuReaderSettings = TtsuReaderSettings(),
)