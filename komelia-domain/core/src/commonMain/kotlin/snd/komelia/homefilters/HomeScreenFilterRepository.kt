package snd.komelia.homefilters

import kotlinx.coroutines.flow.Flow

interface HomeScreenFilterRepository {
    fun getFilters(): Flow<List<HomeScreenFilter>>
    suspend fun putFilters(filters: List<HomeScreenFilter>)
}