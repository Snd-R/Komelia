package io.github.snd_r.komelia.ui.settings.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.dialogs.user.PasswordChangeDialog
import io.github.snd_r.komga.user.KomgaUser

@Composable
fun AccountSettingsContent(user: KomgaUser) {
    Column(
        modifier = Modifier.padding(horizontal = 10.dp).fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text("Account Settings", style = MaterialTheme.typography.titleLarge)
        EmailDetails(user)
        RolesDetails(user)
        HorizontalDivider()
        PasswordDetails(user)
    }

}

@Composable
private fun EmailDetails(user: KomgaUser) {
    Column {
        Text("Email:", fontWeight = FontWeight.Bold)
        Text(user.email, modifier = Modifier.padding(start = 10.dp, top = 5.dp))
    }
}

@Composable
private fun RolesDetails(user: KomgaUser) {
    Column {
        Text("Roles:", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
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
        shape = RoundedCornerShape(5.dp)
    ) {
        Text("CHANGE PASSWORD")
    }
    if (showPasswordDialog) {
        PasswordChangeDialog(user = user, onDismiss = { showPasswordDialog = false })
    }
}

