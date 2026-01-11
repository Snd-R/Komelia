package snd.komelia.db.homescreen

import kotlinx.serialization.SerializationException
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
        return try {
            transaction {
                HomeScreenFiltersTable.selectAll()
                    .firstOrNull()?.get(HomeScreenFiltersTable.filters)
                    ?.sortedBy { it.order }
            }
        } catch (_: SerializationException) {
            null
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