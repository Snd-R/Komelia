package snd.komelia.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import io.github.snd_r.komelia.ui.LoadState
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import io.github.snd_r.komelia.ui.error.formatExceptionMessage
import io.github.snd_r.komelia.ui.settings.komf.jobs.KomfJobsContent
import snd.komelia.LocalKomfViewModelFactory

class JobsTab : DialogTab {

    override fun options() = TabItem(
        title = "Job History",
        icon = Icons.Default.History
    )

    @Composable
    override fun Content() {
        val viewModelFactory = LocalKomfViewModelFactory.current
        val vm = remember { viewModelFactory.getKomfJobsViewModel() }
        LaunchedEffect(Unit) { vm.initialize() }

        val state = vm.state.collectAsState().value
        when (state) {
            is LoadState.Error -> Text(formatExceptionMessage(state.exception))
            LoadState.Uninitialized, LoadState.Loading, is LoadState.Success -> KomfJobsContent(
                jobs = vm.jobs,
                totalPages = vm.totalPages,
                currentPage = vm.currentPage,
                onPageChange = vm::loadPage,
                selectedStatus = vm.status,
                onStatusSelect = vm::onStatusSelect,
                getSeries = null,
                onSeriesClick = {},
                onDeleteAll = vm::onDeleteAll,
                isLoading = state == LoadState.Loading || state == LoadState.Uninitialized
            )
        }

    }
}
