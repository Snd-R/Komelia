package io.github.snd_r.komelia.ui.home.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.ErrorContent
import io.github.snd_r.komelia.ui.home.HomeFilterData
import io.github.snd_r.komelia.ui.home.HomeScreen
import io.github.snd_r.komelia.ui.home.edit.view.FilterEditContent
import kotlinx.coroutines.launch

class FilterEditScreen(private val homeFilters: List<HomeFilterData>? = null) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getFilterEditViewModel(homeFilters) }
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            vm.initialize()
        }

        when (val state = vm.state.collectAsState().value) {
            is LoadState.Error -> ErrorContent(
                message = state.exception.message ?: "Unknown Error",
                onExit = { navigator.replaceAll(HomeScreen()) }
            )

            else -> FilterEditContent(
                filters = vm.filters.collectAsState().value,
                onFilterMove = vm::onFilterReorder,
                onEditEnd = {
                    coroutineScope.launch {
                        vm.onEditEnd()
                        navigator.replaceAll(HomeScreen())
                    }
                },
                onFilterAdd = vm::onFilterAdd,
                onFilterRemove = vm::onFilterRemove,
                onFiltersReset = vm::onResetFiltersToDefault
            )
        }
    }
}