package io.github.snd_r.komelia.ui.navigation

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import io.github.snd_r.komelia.ui.common.AppTheme
import io.github.snd_r.komelia.ui.common.menus.LibraryActionsMenu
import io.github.snd_r.komelia.ui.common.menus.LibraryMenuActions
import io.github.snd_r.komelia.ui.dialogs.libraryedit.LibraryEditDialogs
import io.github.snd_r.komelia.ui.library.DashboardScreen
import io.github.snd_r.komelia.ui.library.LibraryScreen
import io.github.snd_r.komelia.platform.VerticalScrollbar
import io.github.snd_r.komga.library.KomgaLibrary
import io.github.snd_r.komga.library.KomgaLibraryId
import io.github.snd_r.komga.sse.KomgaEvent.TaskQueueStatus

@Composable
fun RegularNavBar(
    isOpen: Boolean,
    currentScreen: Screen,
    libraries: List<KomgaLibrary>,
    libraryActions: LibraryMenuActions,
    onHomeClick: () -> Unit,
    onLibrariesClick: () -> Unit,
    onLibraryClick: (KomgaLibraryId) -> Unit,
    onSettingsClick: () -> Unit,
    taskQueueStatus: TaskQueueStatus?,
) {
    if (isOpen) {
        Box(Modifier.width(230.dp)) {
            NavMenu(
                currentScreen = currentScreen,
                libraries = libraries,
                libraryActions = libraryActions,
                onHomeClick = onHomeClick,
                onLibrariesClick = onLibrariesClick,
                onLibraryClick = onLibraryClick,
                onSettingsClick = onSettingsClick
            )
            if (taskQueueStatus != null && taskQueueStatus.count > 0) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    TaskQueueIndicator(taskQueueStatus)
                }
            }
        }
    }
}

@Composable
private fun NavMenu(
    currentScreen: Screen,
    libraries: List<KomgaLibrary>,
    libraryActions: LibraryMenuActions,
    onHomeClick: () -> Unit,
    onLibrariesClick: () -> Unit,
    onLibraryClick: (KomgaLibraryId) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val scrollState: ScrollState = rememberScrollState()
    val navBarInteractionSource = remember { MutableInteractionSource() }
    val isHovered = navBarInteractionSource.collectIsHoveredAsState()
    var showLibraryAddDialog by remember { mutableStateOf(false) }
    if (showLibraryAddDialog) {
        LibraryEditDialogs(
            library = null,
            onDismissRequest = { showLibraryAddDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .hoverable(navBarInteractionSource)
    ) {
        Column(modifier = Modifier.verticalScroll(scrollState)) {


            NavButton(
                onClick = { onHomeClick() },
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen is DashboardScreen
            )

            NavButton(
                onClick = { onLibrariesClick() },
                icon = Icons.AutoMirrored.Filled.LibraryBooks,
                label = "Libraries",
                isSelected = false,
                actionButton = {
                    IconButton(onClick = { showLibraryAddDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                        )
                    }
                })

            libraries.forEach { library ->
                NavButton(
                    onClick = { onLibraryClick(library.id) },
                    icon = null,
                    label = library.name,
                    isSelected = currentScreen is LibraryScreen && currentScreen.libraryId == library.id,
                    actionButton = {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {

                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = null,
                            )

                            LibraryActionsMenu(
                                library = library,
                                actions = libraryActions,
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            )
                        }
                    })
            }

            HorizontalDivider(Modifier.padding(0.dp, 20.dp))
            NavButton(
                onClick = onSettingsClick,
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = false
            )

            Spacer(Modifier.height(20.dp))
        }

        if (isHovered.value) VerticalScrollbar(scrollState, Modifier.align(Alignment.CenterEnd))
    }
}

@Composable
private fun NavButton(
    onClick: () -> Unit,
    icon: ImageVector?,
    label: String,
    actionButton: (@Composable () -> Unit)? = null,
    isSelected: Boolean,
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    if (isSelected) AppTheme.colors.material.surfaceVariant
                    else AppTheme.colors.material.surface
                )
        ) {

            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp, 0.dp, 20.dp, 0.dp)
                )
            } else {
                Box(Modifier.padding(30.dp, 0.dp)) {}
            }

            Text(label, fontSize = 14.sp)

            if (actionButton != null) {
                Spacer(Modifier.weight(1.0f))
                actionButton()
            }

        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TaskQueueIndicator(queueStatus: TaskQueueStatus) {
    BasicTooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .9f)) {
                Column(Modifier.padding(10.dp)) {
                    when (queueStatus.count) {
                        1 -> Text("1 pending task")
                        else -> Text("${queueStatus.count} pending tasks")
                    }
                    Spacer(Modifier.height(10.dp))
                    queueStatus.countByType.forEach { (task, count) ->
                        Text("$task: $count")
                    }
                }
            }
        },
        state = rememberBasicTooltipState()
    ) {
        Box(
            modifier = Modifier.clickable {}.height(20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            LinearProgressIndicator(
                modifier = Modifier.height(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                trackColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
