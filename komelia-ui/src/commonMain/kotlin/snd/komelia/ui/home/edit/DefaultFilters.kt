package snd.komelia.ui.home.edit

import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.home_filter_default_keep_reading
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.home_filter_default_on_deck
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.home_filter_default_recently_added_books
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.home_filter_default_recently_added_series
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.home_filter_default_recently_read_books
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.home_filter_default_recently_realeased_books
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.home_filter_default_recently_updated_series
import org.jetbrains.compose.resources.getString
import snd.komelia.homefilters.BooksHomeScreenFilter
import snd.komelia.homefilters.HomeScreenFilter
import snd.komelia.homefilters.SeriesHomeScreenFilter
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort
import snd.komga.client.search.allOfBooks
import kotlin.time.Duration.Companion.days

suspend fun getDefaultFilters(): List<HomeScreenFilter> {
    return listOf(
        BooksHomeScreenFilter.CustomFilter(
            order = 1,
            label = getString(Res.string.home_filter_default_keep_reading),
            filter = allOfBooks { readStatus { isEqualTo(KomgaReadStatus.IN_PROGRESS) } }.toBookCondition(),
            pageRequest = KomgaPageRequest(sort = KomgaSort.KomgaBooksSort.byReadDateDesc())
        ),
        BooksHomeScreenFilter.OnDeck(
            order = 2,
            label = getString(Res.string.home_filter_default_on_deck),
            pageSize = 20,
        ),
        BooksHomeScreenFilter.CustomFilter(
            order = 3,
            label = getString(Res.string.home_filter_default_recently_realeased_books),
            filter = allOfBooks { releaseDate { isInLast(30.days) } }.toBookCondition(),
            pageRequest = KomgaPageRequest(
                sort = KomgaSort.KomgaBooksSort.byReleaseDateDesc(),
            )
        ),
        BooksHomeScreenFilter.CustomFilter(
            order = 4,
            label = getString(Res.string.home_filter_default_recently_added_books),
            filter = allOfBooks {}.toBookCondition(),
            pageRequest = KomgaPageRequest(
                sort = KomgaSort.KomgaBooksSort.byCreatedDateDesc(),
                size = 20
            )
        ),
        SeriesHomeScreenFilter.RecentlyAdded(
            order = 5,
            label = getString(Res.string.home_filter_default_recently_added_series),
            pageSize = 20,
        ),
        SeriesHomeScreenFilter.RecentlyUpdated(
            order = 6,
            label = getString(Res.string.home_filter_default_recently_updated_series),
            pageSize = 20,
        ),
        BooksHomeScreenFilter.CustomFilter(
            order = 7,
            label = getString(Res.string.home_filter_default_recently_read_books),
            filter = allOfBooks {
                readStatus { isEqualTo(KomgaReadStatus.READ) }
            }.toBookCondition(),
            pageRequest = KomgaPageRequest(sort = KomgaSort.KomgaBooksSort.byReadDateDesc())
        ),
    ).sortedBy { it.order }
}
