package snd.komelia.ui.home.edit

import kotlinx.coroutines.flow.MutableStateFlow
import snd.komelia.homefilters.HomeScreenFilter

sealed interface FilterEditState {
    val label: MutableStateFlow<String>
    fun toFilter(order: Int): HomeScreenFilter
}