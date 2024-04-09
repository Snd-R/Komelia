package io.github.snd_r.komga.user

import io.github.snd_r.komga.common.KomgaSort

private const val dateTime = "dateTime"

class KomgaUserSort private constructor(orders: List<Order>) : KomgaSort(orders) {

    fun and(sort: KomgaUserSort): KomgaUserSort {
        val these = this.toMutableList()
        sort.forEach { these.add(it) }

        return KomgaUserSort(these)
    }

    fun by(vararg orders: Order): KomgaUserSort {
        return KomgaUserSort(orders.toList())
    }

    companion object {

        fun byDateTimeAsc(): KomgaUserSort {
            return KomgaUserSort(listOf(Order(dateTime, Direction.ASC)))
        }

        fun byDateTimeDesc(): KomgaUserSort {
            return KomgaUserSort(listOf(Order(dateTime, Direction.DESC)))
        }

    }
}
