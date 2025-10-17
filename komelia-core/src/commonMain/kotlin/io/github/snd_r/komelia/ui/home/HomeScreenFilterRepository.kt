package io.github.snd_r.komelia.ui.home

import kotlinx.coroutines.flow.Flow

interface HomeScreenFilterRepository {
    fun getFilters(): Flow<List<HomeScreenFilter>>
    suspend fun putFilters(filters: List<HomeScreenFilter>)
}