package io.github.snd_r.komga.book

import io.github.snd_r.komga.common.KomgaSort

private const val created = "createdDate"
private const val filename = "name"
private const val fileSize = "fileSize"
private const val lastModified = "lastModified"
private const val number = "metadata.numberSort"
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

        fun byCreatedDateAsc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(created, Direction.ASC)))
        }

        fun byCreatedDateDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(created, Direction.DESC)))
        }

        fun byFileNameAsc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(filename, Direction.ASC)))
        }

        fun byFileNameDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(filename, Direction.DESC)))
        }

        fun byLastModifiedDateDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(lastModified, Direction.DESC)))
        }

        fun byNumberAsc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(number, Direction.ASC)))
        }

        fun byNumberDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(number, Direction.DESC)))
        }

        fun byReadDateDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(readDate, Direction.DESC)))
        }

        fun byReleaseDateAsc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(releaseDate, Direction.ASC)))
        }

        fun byReleaseDateDesc(): KomgaBooksSort {
            return KomgaBooksSort(listOf(Order(releaseDate, Direction.DESC)))
        }
    }


}