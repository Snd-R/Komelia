package io.github.snd_r.komelia.ui.home.edit

import io.github.snd_r.komelia.ui.home.HomeScreenFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface FilterEditState {
    val label: MutableStateFlow<String>
    fun toFilter(order: Int): HomeScreenFilter
}