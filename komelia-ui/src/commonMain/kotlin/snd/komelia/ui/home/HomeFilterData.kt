package snd.komelia.ui.home

import snd.komelia.homefilters.BooksHomeScreenFilter
import snd.komelia.homefilters.HomeScreenFilter
import snd.komelia.homefilters.SeriesHomeScreenFilter
import snd.komelia.komga.api.model.KomeliaBook
import snd.komga.client.series.KomgaSeries

sealed interface HomeFilterData {
    val filter: HomeScreenFilter
}

data class SeriesFilterData(
    val series: List<KomgaSeries>,
    override val filter: SeriesHomeScreenFilter,
) : HomeFilterData

data class BookFilterData(
    val books: List<KomeliaBook>,
    override val filter: BooksHomeScreenFilter,
) : HomeFilterData
