package io.github.snd_r.komelia.ui.collection

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.snd_r.komelia.platform.BackPressHandler
import io.github.snd_r.komelia.ui.LoadState.Error
import io.github.snd_r.komelia.ui.LoadState.Loading
import io.github.snd_r.komelia.ui.LoadState.Success
import io.github.snd_r.komelia.ui.LoadState.Uninitialized
import io.github.snd_r.komelia.ui.LocalViewModelFactory
import io.github.snd_r.komelia.ui.common.LoadingMaxSizeIndicator
import io.github.snd_r.komelia.ui.series.SeriesScreen
import io.github.snd_r.komga.collection.KomgaCollectionId

class CollectionScreen(val collectionId: KomgaCollectionId) : Screen {

    @Composable
    override fun Content() {
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel(collectionId.value) { viewModelFactory.getCollectionViewModel(collectionId) }
        LaunchedEffect(collectionId) {
            vm.initialize()
        }
        val navigator = LocalNavigator.currentOrThrow

        when (vm.state.collectAsState().value) {
            Uninitialized -> LoadingMaxSizeIndicator()
            is Success, Loading -> CollectionContent(
                collection = vm.collection,
                onCollectionDelete = vm::onCollectionDelete,

                series = vm.series,
                seriesActions = vm.seriesMenuActions(),
                onSeriesClick = { navigator push SeriesScreen(it.id) },

                selectedSeries = vm.selectedSeries,
                onSeriesSelect = vm::onSeriesSelect,

                editMode = vm.isInEditMode,
                onEditModeChange = vm::setEditMode,
                onReorder = vm::onSeriesReorder,
                onReorderDragStateChange = vm::onSeriesReorderDragStateChange,

                totalSeriesCount = vm.totalSeriesCount,
                totalPages = vm.totalSeriesPages,
                currentPage = vm.currentSeriesPage,
                pageSize = vm.pageLoadSize,
                onPageChange = vm::onPageChange,
                onPageSizeChange = vm::onPageSizeChange,

                onBackClick = { navigator.pop() },
                cardMinSize = vm.cardWidth.collectAsState().value,
            )

            is Error -> Text("Error")
        }

        BackPressHandler { navigator.pop() }

    }
}