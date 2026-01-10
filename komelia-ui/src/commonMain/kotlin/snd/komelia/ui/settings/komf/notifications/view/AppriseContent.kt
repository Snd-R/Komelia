package snd.komelia.ui.settings.komf.notifications.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.ktor.http.*
import snd.komelia.ui.LocalWindowWidth
import snd.komelia.ui.common.components.CheckboxWithLabel
import snd.komelia.ui.common.components.SwitchWithLabel
import snd.komelia.ui.dialogs.AppDialog
import snd.komelia.ui.platform.WindowSizeClass.COMPACT
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.settings.komf.notifications.NotificationContextState

@Composable
fun AppriseContent(
    urls: List<String>,
    onUrlAdd: (String) -> Unit,
    onUrlRemove: (String) -> Unit,
    uploadSeriesCover: Boolean,
    onUploadSeriesCoverChange: (Boolean) -> Unit,

    titleTemplate: String,
    onTitleTemplateChange: (String) -> Unit,
    bodyTemplate: String,
    onBodyTemplateChange: (String) -> Unit,

    notificationContextState: NotificationContextState,
    onTemplateSend: () -> Unit,
    onTemplateSave: () -> Unit,
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        Text("Urls")
        urls.forEach { url ->

            Row {
                TextField(
                    value = url,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { onUrlRemove(url) }, modifier = Modifier.cursorForHand()) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
        var showAddUrlDialog by remember { mutableStateOf(false) }
        FilledTonalButton(
            onClick = { showAddUrlDialog = true },
            modifier = Modifier.cursorForHand()
        ) {
            Text("Add Url")
        }
        SwitchWithLabel(
            checked = uploadSeriesCover,
            onCheckedChange = onUploadSeriesCoverChange,
            label = { Text("Upload series cover") }
        )

        if (showAddUrlDialog) {
            AddUrlDialog(
                onDismissRequest = { showAddUrlDialog = false },
                onUrlAdd = onUrlAdd
            )
        }

        HorizontalDivider()
        TemplatesEditor(
            titleTemplate = titleTemplate,
            onTitleTemplateChange = onTitleTemplateChange,
            bodyTemplate = bodyTemplate,
            onBodyTemplateChange = onBodyTemplateChange,
            notificationContextState = notificationContextState,
            onTemplateSend = onTemplateSend,
            onTemplateSave = onTemplateSave
        )

        Spacer(Modifier.height(30.dp))
    }
}

@Composable
fun AddUrlDialog(
    onDismissRequest: () -> Unit,
    onUrlAdd: (String) -> Unit,
) {
    var newWebhook by remember { mutableStateOf("") }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState().value

    val isValidUrl = derivedStateOf { parseUrl(newWebhook) != null }
    val isError by derivedStateOf { newWebhook.isNotBlank() && (!isValidUrl.value) }
    var confirmInvalidUrl by remember(isError) { mutableStateOf(false) }

    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        onDismissRequest = onDismissRequest,
        header = {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add URL", style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider()
            }
        },
        content = {
            Column(
                modifier = Modifier.padding(10.dp).fillMaxWidth(),
            ) {
                TextField(
                    value = newWebhook,
                    onValueChange = { newWebhook = it },
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError,
                    interactionSource = interactionSource,
                    supportingText = { if (isError) Text("failed to parse URL") },
                    visualTransformation = if (isFocused) VisualTransformation.None else PasswordVisualTransformation(),
                )
                if (isError) {
                    CheckboxWithLabel(
                        checked = confirmInvalidUrl,
                        onCheckedChange = { confirmInvalidUrl = !confirmInvalidUrl },
                        label = { Text("apply anyway") }
                    )
                }
            }
        },
        controlButtons = {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {

                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.cursorForHand(),
                    content = { Text("Cancel") }
                )

                FilledTonalButton(
                    onClick = {
                        onUrlAdd(newWebhook)
                        onDismissRequest()
                    },
                    modifier = Modifier.cursorForHand(),
                    enabled = !isError || confirmInvalidUrl
                ) {
                    Text("Confirm")
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplatesEditor(
    titleTemplate: String,
    onTitleTemplateChange: (String) -> Unit,
    bodyTemplate: String,
    onBodyTemplateChange: (String) -> Unit,

    notificationContextState: NotificationContextState,
    onTemplateSend: () -> Unit,
    onTemplateSave: () -> Unit,
) {
    var showNotificationContextDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Notification Template", style = MaterialTheme.typography.titleLarge)
        Column {
            Text("Uses Apprise executable installed on the system.\nTemplates are rendered using Apache Velocity")
            Text(
                "Apprise github page",
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://github.com/caronc/apprise")
                }.padding(2.dp).cursorForHand()
            )
            Text(
                "Velocity Template Language syntax reference",
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://velocity.apache.org/engine/2.3/vtl-reference.html")
                }.padding(2.dp).cursorForHand()
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TextField(
                value = titleTemplate,
                onValueChange = onTitleTemplateChange,
                label = { Text("Title") },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            TextField(
                value = bodyTemplate,
                onValueChange = onBodyTemplateChange,
                label = { Text("Body") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            if (LocalWindowWidth.current != COMPACT) {
                Spacer(Modifier.weight(1f))
            }
            ElevatedButton(
                onClick = { showNotificationContextDialog = true },
                modifier = Modifier.cursorForHand()
            ) {
                Text("Notification Context")

            }

            ElevatedButton(
                onClick = onTemplateSend,
                modifier = Modifier.cursorForHand()
            ) {
                Text("Test Send")
            }

            FilledTonalButton(
                onClick = onTemplateSave,
                enabled = true,
                modifier = Modifier.cursorForHand()
            ) {
                Text("Save")
            }
        }
    }

    if (showNotificationContextDialog) {
        NotificationContextDialog(
            notificationContextState,
            onDismissRequest = { showNotificationContextDialog = false })
    }
}
