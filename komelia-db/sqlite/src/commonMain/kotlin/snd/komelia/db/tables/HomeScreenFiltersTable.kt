package snd.komelia.db.tables

import io.github.snd_r.komelia.ui.home.HomeScreenFilter
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.json.json
import snd.komelia.db.JsonDbDefault

object HomeScreenFiltersTable : Table("HomeScreenFilters") {
    val version = integer("version")
    val filters = json<List<HomeScreenFilter>>("filters", JsonDbDefault)

    override val primaryKey = PrimaryKey(version)
}