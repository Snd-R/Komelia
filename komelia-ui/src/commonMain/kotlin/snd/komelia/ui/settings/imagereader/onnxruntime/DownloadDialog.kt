package snd.komelia.ui.settings.imagereader.onnxruntime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import snd.komelia.formatDecimal
import snd.komelia.ui.dialogs.AppDialog
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.updates.UpdateProgress

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

@Composable
internal fun UpdateProgressContent(
    progress: UpdateProgress
) {
    Column(
        Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (progress.total == 0L) {
            LinearProgressIndicator(modifier = Modifier.fillMaxSize())
            progress.description?.let { Text(it) }
        } else {
            LinearProgressIndicator(
                progress = { progress.completed / progress.total.toFloat() },
                modifier = Modifier.fillMaxSize()
            )
            progress.description?.let { Text(it) }

            val totalMb = remember(progress.total) {
                (progress.total.toFloat() / 1024 / 1024).formatDecimal(2)
            }
            val completedMb = remember(progress.completed) {
                (progress.completed.toFloat() / 1024 / 1024).formatDecimal(2)
            }
            Text("${completedMb}MiB / ${totalMb}MiB")
        }
    }
}
