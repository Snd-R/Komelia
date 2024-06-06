package io.github.snd_r.komelia.ui.dialogs.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.updates.AppRelease

@Composable
fun UpdateDialog(
    newRelease: AppRelease,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        header = { HeaderContent() },
        content = { DialogContent(newRelease) },
        controlButtons = {
            ControlButtons(
                onDismiss = onDismiss,
                onConfirm = onConfirm
            )
        },
        onDismissRequest = {}
    )
}

@Composable
private fun HeaderContent() {

    Column(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("New version is available", style = MaterialTheme.typography.headlineSmall)
        HorizontalDivider()
    }
}

@Composable
private fun DialogContent(release: AppRelease) {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(release.version.toString(), style = MaterialTheme.typography.titleLarge)
        val state = rememberRichTextState()
        state.config.apply {
            linkColor = MaterialTheme.colorScheme.secondary
            linkTextDecoration = TextDecoration.Underline
            codeSpanBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
            codeSpanStrokeColor = MaterialTheme.colorScheme.surfaceVariant
        }
        state.setMarkdown(release.releaseNotesBody)
        RichText(state)
    }

}

@Composable
private fun ControlButtons(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier.padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        TextButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.cursorForHand(),
            content = { Text("Dismiss") }
        )

        FilledTonalButton(
            onClick = onConfirm,
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.cursorForHand(),
        ) {
            Text("Update")
        }
    }
}

