package snd.komelia.db.repository

import io.github.snd_r.komelia.settings.KomfSettingsRepository
import kotlinx.coroutines.flow.Flow
import snd.komelia.db.KomfSettings
import snd.komelia.db.SettingsStateActor

class ActorKomfSettingsRepository(
    private val actor: SettingsStateActor<KomfSettings>,
) : KomfSettingsRepository {

    override fun getKomfEnabled(): Flow<Boolean> {
        return actor.mapState { it.enabled }
    }

    override suspend fun putKomfEnabled(enabled: Boolean) {
        actor.transform { it.copy(enabled = enabled) }
    }

    override fun getKomfUrl(): Flow<String> {
        return actor.mapState { it.remoteUrl }
    }

    override suspend fun putKomfUrl(url: String) {
        actor.transform { it.copy(remoteUrl = url) }
    }

}