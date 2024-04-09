package io.github.snd_r.komga.book

import io.github.snd_r.komga.common.KomgaSort

private const val titleSort = "metadata.titleSort"
private const val created = "createdDate"
private const val lastModified = "lastModified"
private const val readDate = "readProgress.readDate"
private const val releaseDate = "metadata.releaseDate"

class KomgaBooksSort private constructor(orders: List<Order>) : KomgaSort(orders) {

    fun and(sort: KomgaBooksSort): KomgaBooksSort {
        val these = this.toMutableList()
        sort.forEach { these.add(it) }

        return KomgaBooksSort(these)
    }

    fun by(vararg orders: Order): KomgaBooksSort {
        return KomgaBooksSort(orders.toList())
    }

    companion object {

        fun byTitleAsc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(titleSort, Direction.ASC)))
        }

        fun byTitleDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(titleSort, Direction.DESC)))
        }

        fun byCreatedDateDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(created, Direction.DESC)))
        }

        fun byLastModifiedDateDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(lastModified, Direction.DESC)))
        }

        fun byReadDateDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(readDate, Direction.DESC)))
        }

        fun byReleaseDateDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(releaseDate, Direction.DESC)))
        }
    }


}