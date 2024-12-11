package snd.komelia.db

import io.github.snd_r.komelia.ui.reader.epub.TtsuReaderSettings
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

@Serializable
data class EpubReaderSettings(
    val readerType: EpubReaderType = EpubReaderType.TTSU_EPUB,
    val komgaReaderSettings: JsonObject = buildJsonObject { },
    val ttsuReaderSettings: TtsuReaderSettings = TtsuReaderSettings(),
)