package snd.komelia.homefilters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import snd.komga.client.book.KomgaReadStatus
import snd.komga.client.common.KomgaPageRequest
import snd.komga.client.common.KomgaSort
import snd.komga.client.search.KomgaSearchCondition
import snd.komga.client.search.allOfBooks
import kotlin.time.Duration.Companion.days


@Serializable
sealed interface HomeScreenFilter {
    val order: Int
    val label: String

    fun withOrder(newOrder: Int): HomeScreenFilter
}

@Serializable
sealed interface SeriesHomeScreenFilter : HomeScreenFilter {
    override fun withOrder(newOrder: Int): SeriesHomeScreenFilter

    @Serializable
    @SerialName("io.github.snd_r.komelia.ui.home.SeriesHomeScreenFilter.RecentlyAdded")
    data class RecentlyAdded(
        override val order: Int,
        override val label: String,
        val pageSize: Int,
    ) : SeriesHomeScreenFilter {
        override fun withOrder(newOrder: Int) = this.copy(order = newOrder)
    }

    @Serializable
    @SerialName("io.github.snd_r.komelia.ui.home.SeriesHomeScreenFilter.RecentlyUpdated")
    data class RecentlyUpdated(
        override val order: Int,
        override val label: String,
        val pageSize: Int,
    ) : SeriesHomeScreenFilter {
        override fun withOrder(newOrder: Int) = this.copy(order = newOrder)
    }

    @Serializable
    @SerialName("io.github.snd_r.komelia.ui.home.SeriesHomeScreenFilter.CustomFilter")
    data class CustomFilter(
        override val order: Int,
        override val label: String,
        val filter: KomgaSearchCondition.SeriesCondition? = null,
        val textSearch: String? = null,
        val pageRequest: KomgaPageRequest? = null,
    ) : SeriesHomeScreenFilter {
        override fun withOrder(newOrder: Int) = this.copy(order = newOrder)
    }
}

@Serializable
sealed interface BooksHomeScreenFilter : HomeScreenFilter {
    override fun withOrder(newOrder: Int): BooksHomeScreenFilter

    @Serializable
    @SerialName("io.github.snd_r.komelia.ui.home.BooksHomeScreenFilter.OnDeck")
    data class OnDeck(
        override val order: Int,
        override val label: String,
        val pageSize: Int,
    ) : BooksHomeScreenFilter {
        override fun withOrder(newOrder: Int) = this.copy(order = newOrder)
    }

    @Serializable
    @SerialName("io.github.snd_r.komelia.ui.home.BooksHomeScreenFilter.CustomFilter")
    data class CustomFilter(
        override val order: Int,
        override val label: String,
        val filter: KomgaSearchCondition.BookCondition? = null,
        val textSearch: String? = null,
        val pageRequest: KomgaPageRequest? = null,
    ) : BooksHomeScreenFilter {
        override fun withOrder(newOrder: Int) = this.copy(order = newOrder)
    }
}
