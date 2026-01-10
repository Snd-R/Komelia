package snd.komelia.ui.settings.imagereader.onnxruntime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import snd.komelia.image.UpscaleMode
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.updates.UpdateProgress

@Composable
fun UpscalerSettings(
    upscaleMode: UpscaleMode,
    onModeChange: (UpscaleMode) -> Unit,
    tileSize: Int,
    onTileSizeChange: (Int) -> Unit,
    userModelPath: PlatformFile?,
    onModelPathChange: (PlatformFile?) -> Unit,
    isMangaJaNaiDownloaded: Boolean,
    onMangaJaNaiDownload: () -> Flow<UpdateProgress>,
) {

    UpscaleModeSelector(
        currentMode = upscaleMode,
        onModeChange = onModeChange,
        currentModelPath = userModelPath,
        onModelPathChange = onModelPathChange
    )


    TileSizeSelector(
        tileSize = tileSize,
        onTileSizeChange = onTileSizeChange
    )

    HorizontalDivider()
    MangaJaNaiSettings(
        startDownloadFlow = onMangaJaNaiDownload,
        isInstalled = isMangaJaNaiDownloaded
    )
}

@Composable
fun MangaJaNaiSettings(
    startDownloadFlow: () -> Flow<UpdateProgress>,
    isInstalled: Boolean,
) {
    var showMangaJaNaiDownloadDialog by remember { mutableStateOf(false) }
    if (showMangaJaNaiDownloadDialog) {
        DownloadDialog(
            headerText = "Downloading MangaJaNai ONNX models",
            onDownloadRequest = startDownloadFlow,
            onDismiss = { showMangaJaNaiDownloadDialog = false },
        )
    }

    val uriHandler = LocalUriHandler.current
    Column {
        Text("MangaJaNai ONNX models preset", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(5.dp))
        Text(
            """
                MangaJaNai is a collection of upscaling models for manga.
                The models are mainly optimized to upscale digital manga images of Japanese or English text with height ranging from around 1200px to 2048px.
            """.trimIndent(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 5.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { showMangaJaNaiDownloadDialog = true },
                modifier = Modifier.cursorForHand()
            ) {
                Text(if (isInstalled) "Re-download MangaJaNai preset" else "Download MangaJaNai preset")
            }

            ElevatedButton(
                onClick = { uriHandler.openUri("https://github.com/the-database/mangajanai") },
                modifier = Modifier.cursorForHand()
            ) {
                Text("Project on Github")
            }
        }
    }
}
