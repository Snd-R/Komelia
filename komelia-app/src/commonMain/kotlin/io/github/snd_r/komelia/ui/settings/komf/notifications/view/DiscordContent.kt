package io.github.snd_r.komelia.ui.settings.komf.notifications.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.HttpTextField
import io.github.snd_r.komelia.ui.common.StateHolder
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.ui.settings.komf.notifications.DiscordState.EmbedFieldState
import io.github.snd_r.komelia.ui.settings.komf.notifications.NotificationContextState
import io.ktor.http.*
import snd.komf.api.notifications.EmbedField
import kotlin.math.max

@Composable
fun DiscordNotificationsContent(
    discordUploadSeriesCover: StateHolder<Boolean>,
    discordWebhooks: List<String>,
    onDiscordWebhookAdd: (String) -> Unit,
    onDiscordWebhookRemove: (String) -> Unit,

    titleTemplate: StateHolder<String>,
    titleUrlTemplate: StateHolder<String>,
    descriptionTemplate: StateHolder<String>,
    fieldTemplates: List<EmbedFieldState>,
    onFieldAdd: () -> Unit,
    onFieldDelete: (EmbedFieldState) -> Unit,
    footerTemplate: StateHolder<String>,

    titlePreview: String,
    titleUrlPreview: String,
    descriptionPreview: String,
    fieldPreviews: List<EmbedField>,
    footerPreview: String,

    notificationContextState: NotificationContextState,
    onTemplateRender: () -> Unit,
    onTemplateSend: () -> Unit,
    onTemplateSave: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {

        Text("Webhooks")
        discordWebhooks.forEach { webhook ->
            Row {
                TextField(
                    value = webhook,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { onDiscordWebhookRemove(webhook) }, modifier = Modifier.cursorForHand()) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }

        var showAddWebhookDialog by remember { mutableStateOf(false) }

        FilledTonalButton(
            onClick = { showAddWebhookDialog = true },
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.cursorForHand()
        ) {
            Text("Add Webhook")
        }

        SwitchWithLabel(
            checked = discordUploadSeriesCover.value,
            onCheckedChange = { discordUploadSeriesCover.setValue(it) },
            label = { Text("Upload series cover") }
        )

        if (showAddWebhookDialog) {
            AddDiscordWebhookDialog(
                onDismissRequest = { showAddWebhookDialog = false },
                onWebhookAdd = onDiscordWebhookAdd
            )
        }

        HorizontalDivider()

        TemplatesContent(
            titleTemplate = titleTemplate,
            titleUrlTemplate = titleUrlTemplate,
            descriptionTemplate = descriptionTemplate,
            fieldTemplates = fieldTemplates,
            onFieldAdd = onFieldAdd,
            onFieldDelete = onFieldDelete,
            footerTemplate = footerTemplate,

            titlePreview = titlePreview,
            titleUrlPreview = titleUrlPreview,
            descriptionPreview = descriptionPreview,
            fieldPreview = fieldPreviews,
            footerPreview = footerPreview,

            notificationContextState = notificationContextState,
            onTemplateSend = onTemplateSend,
            onTemplateSave = onTemplateSave,
            onTemplateRender = onTemplateRender
        )

        Spacer(Modifier.height(30.dp))
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

@Composable
private fun TemplatesContent(
    titleTemplate: StateHolder<String>,
    titleUrlTemplate: StateHolder<String>,
    descriptionTemplate: StateHolder<String>,
    fieldTemplates: List<EmbedFieldState>,
    onFieldAdd: () -> Unit,
    onFieldDelete: (EmbedFieldState) -> Unit,
    footerTemplate: StateHolder<String>,

    titlePreview: String,
    titleUrlPreview: String,
    descriptionPreview: String,
    fieldPreview: List<EmbedField>,
    footerPreview: String,

    notificationContextState: NotificationContextState,
    onTemplateSend: () -> Unit,
    onTemplateSave: () -> Unit,
    onTemplateRender: () -> Unit,
) {
    var showNotificationContextDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Notification Template", style = MaterialTheme.typography.titleLarge)
        Column {
            Text("Uses markdown syntax. Templates are rendered using Apache Velocity")
            Text(
                "Discord Markdown Text 101",
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://support.discord.com/hc/en-us/articles/210298617-Markdown-Text-101-Chat-Formatting-Bold-Italic-Underline")
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

        var selectedTab by remember { mutableStateOf(0) }
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
            ) {
                Text("Write")
            }
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    onTemplateRender()
                    selectedTab = 1
                },
                modifier = Modifier.heightIn(min = 40.dp).cursorForHand(),
            ) {
                Text("Preview")
            }
        }

        Layout(content = {
            TemplatesEditor(
                titleTemplate = titleTemplate,
                titleUrlTemplate = titleUrlTemplate,
                descriptionTemplate = descriptionTemplate,
                fieldTemplates = fieldTemplates,
                onFieldAdd = onFieldAdd,
                onFieldDelete = onFieldDelete,
                footerTemplate = footerTemplate
            )
            if (selectedTab == 1) {
                Surface(modifier = Modifier.background(MaterialTheme.colorScheme.surface).fillMaxHeight()) {
                    Column {
                        TemplatesPreview(
                            titlePreview = titlePreview,
                            titleUrlPreview = titleUrlPreview,
                            descriptionPreview = descriptionPreview,
                            fieldPreview = fieldPreview,
                            footerPreview = footerPreview
                        )
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        ) { measurables, constraints ->
            val editor = measurables[0].measure(constraints.copy(minHeight = 0))
            val preview = measurables.getOrNull(1)?.measure(
                constraints.copy(minHeight = 0, maxHeight = editor.height)
            )
            val maxHeight = preview?.height?.let { max(editor.height, it) } ?: editor.height
            layout(constraints.maxWidth, maxHeight) {
                editor.placeRelative(0, 0)
                preview?.placeRelative(0, 0)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Spacer(Modifier.weight(1f))
            ElevatedButton(
                onClick = { showNotificationContextDialog = true },
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text("Notification Context")

            }

            ElevatedButton(
                onClick = onTemplateSend,
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text("Test Send")
            }

            FilledTonalButton(
                onClick = onTemplateSave,
                enabled = true,
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text("Save")
            }
        }
    }
    if (showNotificationContextDialog) {
        NotificationContextDialog(
            notificationContextState,
            onDismissRequest = {
                showNotificationContextDialog = false
                onTemplateRender()
            })
    }


}

@Composable
private fun TemplatesEditor(
    titleTemplate: StateHolder<String>,
    titleUrlTemplate: StateHolder<String>,
    descriptionTemplate: StateHolder<String>,
    fieldTemplates: List<EmbedFieldState>,
    onFieldAdd: () -> Unit,
    onFieldDelete: (EmbedFieldState) -> Unit,
    footerTemplate: StateHolder<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextField(
            value = titleTemplate.value,
            onValueChange = { titleTemplate.setValue(it) },
            label = { Text("Title. 256 characters max") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        HttpTextField(
            value = titleUrlTemplate.value,
            onValueChange = { titleUrlTemplate.setValue(it) },
            label = { Text("Title Url") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = descriptionTemplate.value,
            onValueChange = { descriptionTemplate.setValue(it) },
            label = { Text("Description. 4096 characters max") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        TemplateFieldsEditor(fieldTemplates, onFieldAdd, onFieldDelete)
        TextField(
            value = footerTemplate.value,
            onValueChange = { footerTemplate.setValue(it) },
            label = { Text("Footer. 2048 characters max") },
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TemplateFieldsEditor(
    fieldTemplates: List<EmbedFieldState>,
    onFieldAdd: () -> Unit,
    onFieldDelete: (EmbedFieldState) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        fieldTemplates.forEachIndexed { index, field ->
            var showField by remember { mutableStateOf(false) }
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showField = !showField }.cursorForHand()

                ) {
                    Icon(if (showField) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                    Text("Field ${index + 1}")
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { onFieldDelete(field) }) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
                AnimatedVisibility(
                    visible = showField,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    TemplateFieldEditor(field)
                }
                HorizontalDivider()
            }

        }

        FilledTonalButton(
            onClick = onFieldAdd,
            shape = RoundedCornerShape(5.dp),
            enabled = fieldTemplates.size < 25,
            modifier = Modifier.cursorForHand()
        ) {
            Text("Add Field")
        }
    }

}

@Composable
private fun TemplateFieldEditor(
    state: EmbedFieldState
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = state.nameTemplate,
                onValueChange = { state.nameTemplate = it },
                label = { Text("Field name. 256 characters max") },
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            CheckboxWithLabel(
                checked = state.inline,
                onCheckedChange = { state.inline = it },
                label = { Text("Inline") })
        }
        TextField(
            value = state.valueTemplate,
            onValueChange = { state.valueTemplate = it },
            label = { Text("Field value. 1024 characters max") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
    }

}

@Composable
private fun TemplatesPreview(
    titlePreview: String,
    titleUrlPreview: String,
    descriptionPreview: String,
    fieldPreview: List<EmbedField>,
    footerPreview: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(5.dp)
    ) {
        Layout(content = {
            PreviewContent(
                titlePreview = titlePreview,
                titleUrlPreview = titleUrlPreview,
                descriptionPreview = descriptionPreview,
                fieldPreview = fieldPreview,
                footerPreview = footerPreview
            )
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(MaterialTheme.colorScheme.secondary)
            )

        }) { measurables, constraints ->
            val preview = measurables[0].measure(constraints)
            val colorSpacer = measurables[1].measure(constraints.copy(maxHeight = preview.height))

            layout(constraints.maxWidth, preview.height) {
                colorSpacer.placeRelative(0, 0)
                preview.placeRelative(colorSpacer.width, 0)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreviewContent(
    titlePreview: String,
    titleUrlPreview: String,
    descriptionPreview: String,
    fieldPreview: List<EmbedField>,
    footerPreview: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val linkColor = MaterialTheme.colorScheme.secondary
        if (titlePreview.isNotBlank()) {
            val titleState = remember(titlePreview, titleUrlPreview) {

                RichTextState().apply {
                    config.linkColor = linkColor
                    config.linkTextDecoration = TextDecoration.Underline
                    setMarkdown(if (titleUrlPreview.isNotBlank()) "[$titlePreview]($titleUrlPreview)" else titlePreview)
                }
            }
            RichText(
                state = titleState,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (descriptionPreview.isNotBlank()) {
            val contentState =
                remember(descriptionPreview) {
                    RichTextState().apply {
                        config.linkColor = linkColor
                        config.linkTextDecoration = TextDecoration.Underline
                        setMarkdown(descriptionPreview)
                    }
                }
            RichText(
                state = contentState,
            )
        }
        FlowRow(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            fieldPreview.forEach { field ->
                Column(
                    modifier = Modifier.widthIn(min = 200.dp).then(
                        if (!field.inline)
                            Modifier.fillMaxWidth() else Modifier.weight(1f)
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val nameState = remember(field.name) {
                        RichTextState().apply {
                            config.linkColor = linkColor
                            config.linkTextDecoration = TextDecoration.Underline
                            setMarkdown(field.name)
                        }
                    }
                    RichText(
                        state = nameState,
                        fontWeight = FontWeight.Bold
                    )
                    val valueState = remember(field.value) {
                        RichTextState().apply {
                            config.linkColor = linkColor
                            config.linkTextDecoration = TextDecoration.Underline
                            setMarkdown(field.value)
                        }
                    }
                    RichText(valueState)
                }

            }
        }

        if (footerPreview.isNotBlank()) {
            Text(footerPreview, style = MaterialTheme.typography.bodySmall)
        }
    }
}


