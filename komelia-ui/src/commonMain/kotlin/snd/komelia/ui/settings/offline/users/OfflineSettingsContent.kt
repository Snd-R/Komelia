package snd.komelia.ui.settings.offline.users

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import snd.komelia.offline.server.model.OfflineMediaServer
import snd.komelia.offline.server.model.OfflineMediaServerId
import snd.komelia.offline.user.model.OfflineUser
import snd.komelia.ui.dialogs.ConfirmationDialog
import snd.komga.client.user.KomgaUser
import snd.komga.client.user.KomgaUserId

@Composable
fun OfflineUserSettingsContent(
    currentUser: KomgaUser?,
    onlineServerUrl: String?,
    serverUsers: Map<OfflineMediaServer, List<OfflineUser>>,
    isOffline: Boolean,
    goOnline: () -> Unit,
    loginAs: (KomgaUserId) -> Unit,
    onServerDelete: (OfflineMediaServerId) -> Unit,
    onUserDelete: (KomgaUserId) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        Column {
            Text("user: ${currentUser?.email ?: "none"}")
            Text("status: ${if (isOffline) "offline" else "online"}")
            Text("server: ${if (currentUser?.id == OfflineUser.ROOT || onlineServerUrl == null) "none" else onlineServerUrl}")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            val canGoOffline = remember(isOffline, serverUsers, currentUser) {
                when {
                    isOffline -> false
                    currentUser == null -> false
                    else -> serverUsers.values.flatten().map { it.id }.contains(currentUser.id)
                }
            }

            if (isOffline) {
                FilledTonalButton(onClick = { goOnline() }) { Text("Go online") }
            } else if (canGoOffline) {
                FilledTonalButton(onClick = { currentUser?.let { loginAs(it.id) } }) { Text("Go offline as current user") }
            }
        }

        for ((server, users) in serverUsers) {
            ServerCard(
                server = server,
                users = users,
                onServerDelete = onServerDelete,
                goOffline = loginAs,
                onUserDelete = onUserDelete,
                expandByDefault = serverUsers.size == 1
            )
        }

        if (serverUsers.size > 1) {
            RootUserCard({ loginAs(OfflineUser.ROOT) })
        }

    }
}

@Composable
fun ServerCard(
    server: OfflineMediaServer,
    users: List<OfflineUser>,
    onServerDelete: ((OfflineMediaServerId) -> Unit)?,
    goOffline: (KomgaUserId) -> Unit,
    onUserDelete: (KomgaUserId) -> Unit,
    expandByDefault: Boolean,
) {

    var showUsers by remember { mutableStateOf(expandByDefault || users.size == 1) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { showUsers = !showUsers }
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)

    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Text(server.url, textDecoration = TextDecoration.Underline)
            }
            Icon(if (showUsers) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
            Spacer(Modifier.weight(1f))

            if (onServerDelete != null) {
                IconButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(Icons.Default.Delete, null)
                }

                if (showDeleteConfirmation) {
                    ConfirmationDialog(
                        body = "Delete all server data?",
                        confirmText = "Yes, delete all downloaded files and user data",
                        onDialogConfirm = { onServerDelete(server.id) },
                        onDialogDismiss = { showDeleteConfirmation = false }
                    )
                }
            }
        }

        if (showUsers) {
            for (user in users) {
                HorizontalDivider()
                UserCard(
                    user = user,
                    goOffline = goOffline,
                    onUserDelete = onUserDelete,
                )
            }
        }
    }


}

@Composable
private fun UserCard(
    user: OfflineUser,
    goOffline: (KomgaUserId) -> Unit,
    onUserDelete: (KomgaUserId) -> Unit,
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Person,
            null,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(user.email)
        }

        FilledTonalButton(onClick = { goOffline(user.id) }) {
            Text("login")
        }

        IconButton(
            onClick = { showDeleteConfirmation = true },
        ) {
            Icon(Icons.Default.Delete, null)
        }
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            body = "Delete user data?",
            confirmText = "Yes, delete user data and associated read progress",
            onDialogConfirm = { onUserDelete(user.id) },
            onDialogDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
fun RootUserCard(goOffline: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.SupervisorAccount,
                    null,
                    tint = MaterialTheme.colorScheme.tertiaryContainer
                )
                Text("root")
            }

            Text("Special user that has access to all downloaded books")
            Text("Read progress will not be synced")
        }


        FilledTonalButton(onClick = { goOffline() }) {
            Text("login")
        }
    }
}
