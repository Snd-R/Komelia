package snd.komelia.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import snd.komelia.LocalKomfViewModelFactory
import snd.komelia.ui.LoadState
import snd.komelia.ui.dialogs.tabs.DialogTab
import snd.komelia.ui.dialogs.tabs.TabItem
import snd.komelia.ui.error.formatExceptionMessage
import snd.komelia.ui.settings.komf.jobs.KomfJobsContent
import snd.komelia.ui.strings.AppStrings

class JobsTab : DialogTab {

    override fun options() = TabItem(
        title = AppStrings.komfJobs,
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
