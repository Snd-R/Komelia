package io.github.snd_r.komelia.ui.settings.komf.notifications

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.ktor.http.*

@Composable
fun KomfSettingsContent(
    discordUploadSeriesCover: StateHolder<Boolean>,
    discordWebhooks: List<String>,
    onDiscordWebhookAdd: (String) -> Unit,
    onDiscordWebhookRemove: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        SwitchWithLabel(
            checked = discordUploadSeriesCover.value,
            onCheckedChange = { discordUploadSeriesCover.setValue(it) },
            label = { Text("Upload series cover") }
        )
        HorizontalDivider()

        Text("Webhooks")
        discordWebhooks.forEach { webhook ->
            DiscordWebhookField(
                webhook = webhook,
                enabled = false,
                onWebhookChange = {},
                onWebhookRemove = { onDiscordWebhookRemove(webhook) }
            )
        }

        var showAddWebhookDialog by remember { mutableStateOf(false) }
        FilledTonalButton(
            onClick = { showAddWebhookDialog = true },
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.cursorForHand()
        ) {
            Text("Add Webhook")
        }

        if (showAddWebhookDialog) {
            AddDiscordWebhookDialog(
                onDismissRequest = { showAddWebhookDialog = false },
                onWebhookAdd = onDiscordWebhookAdd
            )
        }

    }
}

@Composable
private fun DiscordWebhookField(
    webhook: String,
    enabled: Boolean,
    onWebhookChange: (String) -> Unit,
    onWebhookRemove: () -> Unit
) {

    Row {
        TextField(
            value = webhook,
            onValueChange = onWebhookChange,
            enabled = enabled,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onWebhookRemove, modifier = Modifier.cursorForHand()) {
            Icon(Icons.Default.Delete, null)
        }
    }
}

@Composable
private fun AddDiscordWebhookDialog(
    onDismissRequest: () -> Unit,
    onWebhookAdd: (String) -> Unit,

    ) {
    var newWebhook by remember { mutableStateOf("") }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value

    val isValidUrl = derivedStateOf { parseUrl(newWebhook) != null }
    val isDiscordUrl = derivedStateOf { newWebhook.startsWith("https://discord.com/api/webhooks/") }
    val isError = derivedStateOf { newWebhook.isNotBlank() && (!isValidUrl.value || !isDiscordUrl.value) }

    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        onDismissRequest = onDismissRequest,
        header = {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add New Discord Webhook", style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider()
            }
        },
        content = {
            Column(
                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = newWebhook,
                    onValueChange = { newWebhook = it },
                    label = { Text("Webhook URL") },
                    placeholder = { Text("https://discord.com/api/webhooks/...") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError.value,
                    interactionSource = interactionSource,
                    supportingText = { if (isError.value) Text("Invalid webhook URL") },
                    visualTransformation = if (isFocused) VisualTransformation.None else PasswordVisualTransformation(),
                )
            }
        },
        controlButtons = {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {

                TextButton(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand(),
                    content = { Text("Cancel") }
                )

                FilledTonalButton(
                    onClick = {
                        onWebhookAdd(newWebhook)
                        onDismissRequest()
                    },
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand(),
                    enabled = isValidUrl.value
                ) {
                    Text("Confirm")
                }
            }
        }
    )

}