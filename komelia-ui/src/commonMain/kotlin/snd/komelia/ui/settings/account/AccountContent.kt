package snd.komelia.ui.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import snd.komelia.ui.dialogs.user.PasswordChangeDialog
import snd.komga.client.user.KomgaUser

@Composable
fun AccountSettingsContent(user: KomgaUser) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        EmailDetails(user)
        RolesDetails(user)
        PasswordDetails(user)
    }
}

@Composable
private fun EmailDetails(user: KomgaUser) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Email:  ${user.email}", fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RolesDetails(user: KomgaUser) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Roles:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
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
private fun PasswordDetails(user: KomgaUser) {

    var showPasswordDialog by remember { mutableStateOf(false) }
    FilledTonalButton(
        onClick = { showPasswordDialog = true },
        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
    ) {
        Text("Change Password")
    }
    if (showPasswordDialog) {
        PasswordChangeDialog(user = user, onDismiss = { showPasswordDialog = false })
    }
}

