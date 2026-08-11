package snd.komelia.db.homescreen

import kotlinx.browser.localStorage
import org.w3c.dom.set
import snd.komelia.db.LocalStorageJson
import snd.komelia.homefilters.HomeScreenFilter

const val homeFiltersKey = "homeFilters"

class LocalStorageHomeScreenFilterRepository {
    val json = LocalStorageJson.json

    fun getFilters(): List<HomeScreenFilter>? {
        return localStorage.getItem(homeFiltersKey)
            ?.let { json.decodeFromString<List<HomeScreenFilter>>(it) }
    }

    fun putFilters(filters: List<HomeScreenFilter>) {
        localStorage[homeFiltersKey] = json.encodeToString(filters)
    }
}