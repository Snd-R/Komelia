package snd.komelia.ui.settings.offline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import snd.komelia.ui.LocalViewModelFactory
import snd.komelia.ui.settings.SettingsScreenContainer
import snd.komelia.ui.settings.offline.downloads.OfflineDownloadsContent
import snd.komelia.ui.settings.offline.logs.OfflineLogsContent
import snd.komelia.ui.settings.offline.users.OfflineUserSettingsContent

class OfflineSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val currentNavigator = LocalNavigator.currentOrThrow
        val viewModelFactory = LocalViewModelFactory.current
        val vm = rememberScreenModel { viewModelFactory.getOfflineModeSettingsViewModel() }

        LaunchedEffect(Unit) {
            vm.initialize(currentNavigator)
        }

        SettingsScreenContainer("Offline mode") {
            var selectedTab by rememberSaveable { mutableStateOf(0) }

            SecondaryTabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.heightIn(min = 40.dp).pointerHoverIcon(PointerIcon.Hand),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Default.Person, null)
                        Text("Users")
                    }
                }
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.heightIn(min = 40.dp).pointerHoverIcon(PointerIcon.Hand),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Default.Download, null)
                        Text("Downloads")
                    }
                }

                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.heightIn(min = 40.dp).pointerHoverIcon(PointerIcon.Hand),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Default.Cached, null)
                        Text("Logs")
                    }
                }
            }
            when (selectedTab) {
                0 -> {
                    val userState = vm.usersState
                    OfflineUserSettingsContent(
                        currentUser = userState.currentUser.collectAsState().value,
                        onlineServerUrl = userState.onlineServerUrl.collectAsState().value,
                        isOffline = userState.isOffline.collectAsState(false).value,
                        goOnline = userState::goOnline,
                        loginAs = userState::loginAs,
                        serverUsers = userState.offlineUsers.collectAsState().value,
                        onServerDelete = userState::onServerDelete,
                        onUserDelete = userState::onUserDelete,
                    )
                }

                1 -> {
                    val downloadsState = vm.downloadsSate
                    OfflineDownloadsContent(
                        storageLocation = downloadsState.storageLocation.collectAsState().value,
                        onStorageLocationChange = downloadsState::onStorageLocationChange,
                        onStorageLocationReset = downloadsState::onStorageLocationReset,
                        downloads = downloadsState.downloads.collectAsState().value,
                        onDownloadCancel = downloadsState::onDownloadCancel
                    )
                }

                2 -> {
                    val state = vm.logsState
                    OfflineLogsContent(
                        logs = state.logs.collectAsState().value,
                        totalPages = state.totalPages.collectAsState().value,
                        currentPage = state.pageNumber.collectAsState().value,
                        onPageChange = state::onPageChange,
                        selectedTab = state.tab.collectAsState().value,
                        onTabSelect = state::onTabChange,
                        onDelete = state::onLogsDelete
                    )
                }
            }


        }
    }
}