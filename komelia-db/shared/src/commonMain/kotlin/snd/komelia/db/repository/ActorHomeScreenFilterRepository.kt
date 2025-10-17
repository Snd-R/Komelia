package snd.komelia.db.repository

import io.github.snd_r.komelia.ui.home.HomeScreenFilter
import io.github.snd_r.komelia.ui.home.HomeScreenFilterRepository
import kotlinx.coroutines.flow.Flow
import snd.komelia.db.SettingsStateActor

class ActorHomeScreenFilterRepository(
    private val actor: SettingsStateActor<List<HomeScreenFilter>>,
) : HomeScreenFilterRepository {

    override fun getFilters(): Flow<List<HomeScreenFilter>> {
        return actor.state
    }

    override suspend fun putFilters(filters: List<HomeScreenFilter>) {
        actor.transform { filters }
    }
}