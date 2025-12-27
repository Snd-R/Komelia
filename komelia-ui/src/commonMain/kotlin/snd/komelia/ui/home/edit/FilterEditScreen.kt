package snd.komelia.ui.home.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import snd.komelia.ui.LoadState
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.common.components.ErrorContent
import snd.komelia.ui.home.HomeFilterData
import snd.komelia.ui.home.HomeScreen
import snd.komelia.ui.home.edit.view.FilterEditContent

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