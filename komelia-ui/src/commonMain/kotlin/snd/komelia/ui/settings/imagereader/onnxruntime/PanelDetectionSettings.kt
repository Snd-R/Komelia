package snd.komelia.ui.settings.imagereader.onnxruntime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.updates.UpdateProgress

@Composable
fun PanelDetectionSettings(
    isDownloaded: Boolean,
    onDownloadRequest: () -> Flow<UpdateProgress>
) {
    var showDownloadDialog by remember { mutableStateOf(false) }
    if (showDownloadDialog) {
        DownloadDialog(
            headerText = "Downloading panel detection model",
            onDownloadRequest = onDownloadRequest,
            onDismiss = { showDownloadDialog = false },
        )
    }

    Column {
        Text(
            """
                If model is available, a new "Panels" reader mode will be added.
                In this mode reader will zoom and scroll from panel to panel
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 5.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { showDownloadDialog = true },
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text(if (isDownloaded) "Re-download Model" else "Download Model")
            }
            if (isDownloaded) {
                Text("Installed")
                Icon(Icons.Default.Check, null, tint = Color.Green)
            }
        }
    }
}

