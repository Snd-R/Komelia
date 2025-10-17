package io.github.snd_r.komelia.ui.home

import snd.komga.client.book.KomgaBook
import snd.komga.client.series.KomgaSeries

sealed interface HomeFilterData {
    val filter: HomeScreenFilter
}

data class SeriesFilterData(
    val series: List<KomgaSeries>,
    override val filter: SeriesHomeScreenFilter,
) : HomeFilterData

data class BookFilterData(
    val books: List<KomgaBook>,
    override val filter: BooksHomeScreenFilter,
) : HomeFilterData
