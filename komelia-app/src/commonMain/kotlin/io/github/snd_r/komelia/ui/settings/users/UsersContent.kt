package io.github.snd_r.komelia.ui.settings.users

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.dialogs.ConfirmationDialog
import io.github.snd_r.komelia.ui.dialogs.user.PasswordChangeDialog
import io.github.snd_r.komelia.ui.dialogs.user.UserAddDialog
import io.github.snd_r.komelia.ui.dialogs.user.UserEditDialog
import io.github.snd_r.komga.user.KomgaAuthenticationActivity
import io.github.snd_r.komga.user.KomgaUser
import io.github.snd_r.komga.user.KomgaUserId
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

private val dateTimeFormat = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    dayOfMonth()
    char(' ')
    hour()
    char(':')
    minute()
}

@Composable
fun UsersContent(
    currentUser: KomgaUser,
    users: Map<KomgaUser, KomgaAuthenticationActivity?>,
    onUserDelete: (KomgaUserId) -> Unit,
    onUserReloadRequest: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        users.forEach { (user, activity) ->
            UserCard(
                currentUser = currentUser,
                user = user,
                latestActivity = activity,
                onUserDelete = onUserDelete,
                onUserReloadRequest = onUserReloadRequest,
            )
        }
        var showUserAddDialog by remember { mutableStateOf(false) }

        FilledTonalButton(onClick = { showUserAddDialog = true }, shape = RoundedCornerShape(5.dp)) {
            Text("Add User")
        }

        if (showUserAddDialog) {
            UserAddDialog(onDismiss = { showUserAddDialog = false }, afterConfirm = onUserReloadRequest)
        }
    }
}

@Composable
private fun UserCard(
    currentUser: KomgaUser,
    user: KomgaUser,
    latestActivity: KomgaAuthenticationActivity?,
    onUserDelete: (KomgaUserId) -> Unit,
    onUserReloadRequest: () -> Unit,
) {
    var expandActions by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { expandActions = !expandActions }
            .cursorForHand()
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserInfo(user, latestActivity)

            Spacer(Modifier.weight(1f))
            Icon(if (expandActions) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
        }


        Column(Modifier.animateContentSize()) {
            if (expandActions) {
                UserRoles(user)

                UserActions(
                    currentUser = currentUser,
                    user = user,
                    onUserDelete = onUserDelete,
                    onUserReloadRequest = onUserReloadRequest
                )
            }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserRoles(user: KomgaUser) {
    Column {
        Text("Roles:", fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            user.roles.forEach { role ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(role) }
                )
            }
        }
    }
}

@Composable
private fun UserInfo(
    user: KomgaUser,
    latestActivity: KomgaAuthenticationActivity?
) {
    val isAdmin = remember(user) { user.roleAdmin() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 10.dp)
    ) {
        if (isAdmin)
            Icon(
                Icons.Default.SupervisorAccount,
                null,
                tint = MaterialTheme.colorScheme.tertiaryContainer
            )
        else
            Icon(Icons.Default.Person, null)

        Spacer(Modifier.width(20.dp))

        Column(Modifier.width(300.dp)) {
            Text(user.email)

            val activityText = latestActivity?.let {
                "Latest activity: ${
                    it.dateTime.toLocalDateTime(TimeZone.currentSystemDefault()).format(dateTimeFormat)
                }"
            }
                ?: "No recent activity"
            Text(
                activityText,
                style = MaterialTheme.typography.bodyMedium
            )

        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserActions(
    currentUser: KomgaUser,
    user: KomgaUser,
    onUserDelete: (KomgaUserId) -> Unit,
    onUserReloadRequest: () -> Unit,
) {
    val isSelf = remember { currentUser.id == user.id }

    var showEditDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp)

        if (!isSelf)
            FilledTonalButton(
                onClick = { showEditDialog = true },
                contentPadding = contentPadding
            ) {
                Icon(Icons.Default.Edit, null)
                Spacer(Modifier.width(10.dp))
                Text("Edit User")
            }

        FilledTonalButton(
            onClick = { showChangePasswordDialog = true },
            contentPadding = contentPadding
        ) {
            Icon(Icons.Default.LockReset, null)
            Spacer(Modifier.width(10.dp))
            Text("Change Password")
        }


        if (!isSelf)
            FilledTonalButton(
                onClick = { showDeleteDialog = true },
                contentPadding = contentPadding,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(Modifier.width(10.dp))
                Text("Delete User")
            }
    }

    if (showEditDialog) {
        UserEditDialog(user, onDismiss = { showEditDialog = false }, afterConfirm = onUserReloadRequest)
    }
    if (showChangePasswordDialog) {
        PasswordChangeDialog(
            user = if (user.id != currentUser.id) user else null,
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Delete User",
            body = "The user ${user.email} will be deleted from this server. This cannot be undone. Continue?",
            confirmText = "Yes, delete \"${user.email}\"",
            buttonConfirm = "DELETE",
            buttonConfirmColor = MaterialTheme.colorScheme.errorContainer,
            onDialogConfirm = { onUserDelete(user.id) },
            onDialogDismiss = { showDeleteDialog = false }
        )
    }

}