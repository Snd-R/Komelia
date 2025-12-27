package snd.komelia.db.repository

import kotlinx.coroutines.flow.Flow
import snd.komelia.db.SettingsStateWrapper
import snd.komelia.homefilters.HomeScreenFilter
import snd.komelia.homefilters.HomeScreenFilterRepository

class HomeScreenFilterRepositoryWrapper(
    private val wrapper: SettingsStateWrapper<List<HomeScreenFilter>>,
) : HomeScreenFilterRepository {

    override fun getFilters(): Flow<List<HomeScreenFilter>> {
        return wrapper.state
    }

    override suspend fun putFilters(filters: List<HomeScreenFilter>) {
        wrapper.transform { filters }
    }
}