package io.github.snd_r.komelia.ui.settings.imagereader.onnxruntime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.updates.UpdateProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate

@Composable
fun DownloadDialog(
    onDownloadRequest: () -> Flow<UpdateProgress>,
    onDismiss: () -> Unit,
    headerText: String,
) {
    var progress by remember { mutableStateOf(UpdateProgress(0, 0)) }
    LaunchedEffect(Unit) {
        onDownloadRequest().conflate().collect {
            progress = it
            delay(100)
        }
        onDismiss()
    }

    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        header = {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(headerText, style = MaterialTheme.typography.titleLarge)
                HorizontalDivider(Modifier.padding(top = 10.dp))
            }
        },
        content = { UpdateProgressContent(progress) },
        controlButtons = {
            Box(modifier = Modifier.padding(bottom = 10.dp, end = 10.dp)) {

                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand(),
                    content = { Text("Cancel") }
                )
            }
        },
        onDismissRequest = {}
    )
}

