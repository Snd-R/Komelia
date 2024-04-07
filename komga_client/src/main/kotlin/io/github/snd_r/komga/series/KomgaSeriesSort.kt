package io.github.snd_r.komga.series

import io.github.snd_r.komga.common.KomgaSort
import io.github.snd_r.komga.common.KomgaSort.Direction.ASC
import io.github.snd_r.komga.common.KomgaSort.Direction.DESC

private const val titleSort = "metadata.titleSort"
private const val created = "created"
private const val lastModified = "lastModified"
private const val releaseDate = "booksMetadata.releaseDate"
private const val folderName = "name"
private const val booksCount = "booksCount"

class KomgaSeriesSort private constructor(orders: List<Order>) : KomgaSort(orders) {

    fun and(sort: KomgaSeriesSort): KomgaSeriesSort {
        val these = this.toMutableList()
        sort.forEach { these.add(it) }

        return KomgaSeriesSort(these)
    }

    fun by(vararg orders: Order): KomgaSeriesSort {
        return KomgaSeriesSort(orders.toList())
    }

    companion object {
        val UNSORTED = KomgaSeriesSort(emptyList())
        fun byTitleAsc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(titleSort, ASC)))
        }

        fun byTitleDesc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(titleSort, DESC)))
        }

        fun byCreatedDateAsc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(created, ASC)))
        }

        fun byCreatedDateDesc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(created, DESC)))
        }

        fun byReleaseDateAsc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(releaseDate, ASC)))
        }

        fun byReleaseDateDesc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(releaseDate, DESC)))
        }

        fun byFolderNameAsc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(folderName, ASC)))
        }

        fun byFolderNameDesc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(folderName, DESC)))
        }

        fun byBooksCountAsc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(booksCount, ASC)))
        }

        fun byBooksCountDesc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(booksCount, DESC)))
        }

        fun byLastModifiedDateAsc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(lastModified, ASC)))
        }

        fun byLastModifiedDateDesc(): KomgaSeriesSort {
            return KomgaSeriesSort(listOf(Order(lastModified, DESC)))
        }
    }


}