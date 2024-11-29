package snd.komelia.db.repository

import io.github.snd_r.komelia.settings.EpubReaderSettingsRepository
import io.github.snd_r.komelia.ui.reader.epub.TtsuReaderSettings
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import snd.komelia.db.EpubReaderSettings
import snd.komelia.db.SettingsStateActor

class ActorEpubReaderSettingsRepository(
    private val stateActor: SettingsStateActor<EpubReaderSettings>
) : EpubReaderSettingsRepository {

    override fun getReaderType(): Flow<EpubReaderType> {
        return stateActor.mapState { it.readerType }
    }

    override suspend fun putReaderType(type: EpubReaderType) {
        stateActor.transform { it.copy(readerType = type) }
    }

    override suspend fun getKomgaReaderSettings(): JsonObject {
        return stateActor.state.value.komgaReaderSettings
    }

    override suspend fun putKomgaReaderSettings(settings: JsonObject) {
        return stateActor.transform { it.copy(komgaReaderSettings = settings) }
    }

    override suspend fun getTtsuReaderSettings(): TtsuReaderSettings {
        return stateActor.state.value.ttsuReaderSettings
    }

    override suspend fun putTtsuReaderSettings(settings: TtsuReaderSettings) {
        return stateActor.transform { it.copy(ttsuReaderSettings = settings) }
    }
}