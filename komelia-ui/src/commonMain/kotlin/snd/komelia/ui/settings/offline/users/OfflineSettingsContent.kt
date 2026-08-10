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
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_delete_server_data
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_delete_server_data_confirm
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_delete_user_data
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_delete_user_data_confirm
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_go_offline
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_go_online
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_login_as
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_none_value
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_root
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_root_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_server
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_status
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_status_offline
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_status_online
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_offline_mode_users_user
import org.jetbrains.compose.resources.stringResource
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
            Text(
                stringResource(
                    Res.string.settings_offline_mode_users_user,
                    currentUser?.email ?: stringResource(Res.string.settings_offline_mode_users_none_value)
                )
            )
            Text(
                stringResource(
                    Res.string.settings_offline_mode_users_status,
                    if (isOffline) stringResource(Res.string.settings_offline_mode_users_status_offline)
                    else stringResource(Res.string.settings_offline_mode_users_status_online)
                )
            )
            Text(
                stringResource(
                    Res.string.settings_offline_mode_users_server,
                    if (currentUser?.id == OfflineUser.ROOT || onlineServerUrl == null) stringResource(Res.string.settings_offline_mode_users_none_value)
                    else onlineServerUrl
                )
            )
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
                FilledTonalButton(onClick = { goOnline() }) { Text(stringResource(Res.string.settings_offline_mode_users_go_online)) }
            } else if (canGoOffline) {
                FilledTonalButton(onClick = { currentUser?.let { loginAs(it.id) } }) { Text(stringResource(Res.string.settings_offline_mode_users_go_offline)) }
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
                        body = stringResource(Res.string.settings_offline_mode_users_delete_server_data),
                        confirmText = stringResource(Res.string.settings_offline_mode_users_delete_server_data_confirm),
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
            Text(stringResource(Res.string.settings_offline_mode_users_login_as))
        }

        IconButton(
            onClick = { showDeleteConfirmation = true },
        ) {
            Icon(Icons.Default.Delete, null)
        }
    }

    if (showDeleteConfirmation) {
        ConfirmationDialog(
            body = stringResource(Res.string.settings_offline_mode_users_delete_user_data),
            confirmText = stringResource(Res.string.settings_offline_mode_users_delete_user_data_confirm),
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
                Text(stringResource(Res.string.settings_offline_mode_users_root))
            }

            Text(stringResource(Res.string.settings_offline_mode_users_root_desc))
        }


        FilledTonalButton(onClick = { goOffline() }) {
            Text(stringResource(Res.string.settings_offline_mode_users_login_as))
        }
    }
}
