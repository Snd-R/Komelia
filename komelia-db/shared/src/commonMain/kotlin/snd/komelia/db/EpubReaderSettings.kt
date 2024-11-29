package snd.komelia.db

import io.github.snd_r.komelia.ui.reader.epub.TtsuReaderSettings
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

data class EpubReaderSettings(
    val readerType: EpubReaderType = EpubReaderType.TTSU,
    val komgaReaderSettings: JsonObject = buildJsonObject { },
    val ttsuReaderSettings: TtsuReaderSettings = TtsuReaderSettings(),
)