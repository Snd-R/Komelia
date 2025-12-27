package snd.komelia.db.homescreen

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import snd.komelia.db.ExposedRepository
import snd.komelia.db.tables.HomeScreenFiltersTable
import snd.komelia.homefilters.HomeScreenFilter

class ExposedHomeScreenFilterRepository(
    database: Database
) : ExposedRepository(database) {

    suspend fun getFilters(): List<HomeScreenFilter>? {
        return transaction {
            HomeScreenFiltersTable.selectAll()
                .firstOrNull()?.get(HomeScreenFiltersTable.filters)
                ?.sortedBy { it.order }
        }
    }

    suspend fun putFilters(filters: List<HomeScreenFilter>) {
        transaction {
            HomeScreenFiltersTable.upsert {
                it[this.version] = 1
                it[this.filters] = filters
            }
        }
    }
}