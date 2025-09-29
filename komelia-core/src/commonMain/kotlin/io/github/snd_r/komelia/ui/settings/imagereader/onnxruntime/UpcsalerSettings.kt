package io.github.snd_r.komelia.ui.settings.imagereader.onnxruntime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.image.UpscaleMode
import io.github.snd_r.komelia.image.UpscaleMode.USER_SPECIFIED_MODEL
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.intEntry
import io.github.snd_r.komelia.updates.UpdateProgress
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.flow.Flow
import kotlinx.io.files.Path

@Composable
fun UpscalerSettings(
    upscaleMode: UpscaleMode,
    onModeChange: (UpscaleMode) -> Unit,
    tileSize: Int,
    onTileSizeChange: (Int) -> Unit,
    userModelPath: String?,
    onModelPathChange: (String?) -> Unit,
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
fun UpscaleModeSelector(
    currentMode: UpscaleMode,
    onModeChange: (UpscaleMode) -> Unit,
    currentModelPath: String?,
    onModelPathChange: (String?) -> Unit,
) {
    val strings = LocalStrings.current.imageSettings
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(currentMode, strings.forOnnxRuntimeUpscaleMode(currentMode)),
        options = remember {
            UpscaleMode.entries.map {
                LabeledEntry(it, strings.forOnnxRuntimeUpscaleMode(it))
            }
        },
        onOptionChange = { onModeChange(it.value) },
        label = { Text("OnnxRuntime upscale mode") },
        inputFieldModifier = Modifier.fillMaxSize()
    )
    AnimatedVisibility(currentMode == USER_SPECIFIED_MODEL) {
        val launcher = rememberFilePickerLauncher(
            type = PickerType.File(listOf("onnx")),
            initialDirectory = currentModelPath?.let { Path(it).parent?.toString() },
        ) { file -> file?.path?.let { onModelPathChange(it) } }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 10.dp)
        ) {
            TextField(
                value = currentModelPath ?: "",
                onValueChange = onModelPathChange,
                label = { Text("ONNX model path") },
                readOnly = true,
                modifier = Modifier.weight(7f),
            )

            ElevatedButton(
                onClick = { launcher.launch() },
                modifier = Modifier.padding(horizontal = 10.dp),
                shape = RoundedCornerShape(5.dp)
            ) {
                Text("Browse")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TileSizeSelector(
    tileSize: Int,
    onTileSizeChange: (Int) -> Unit,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DropdownChoiceMenu(
            selectedOption = remember(tileSize) { if (tileSize == 0) LabeledEntry(0, "None") else intEntry(tileSize) },
            options = remember {
                listOf(
                    LabeledEntry(0, "None"),
                    intEntry(4096),
                    intEntry(2048),
                    intEntry(1024),
                    intEntry(512),
                    intEntry(256),
                    intEntry(128)
                )
            },
            onOptionChange = { onTileSizeChange(it.value) },
            label = { Text("Tile size") },
            modifier = Modifier.weight(1f),
            inputFieldModifier = Modifier.fillMaxSize()
        )

        BasicTooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                Card(
                    modifier = Modifier.widthIn(max = 450.dp),
                    border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = """
                            Splits image into small regions of specified size and upscales them individually
                            Upscaled regions are then recombined back into single upscaled image
                            
                            This helps upscaling without running out of VRAM for big images
                            """.trimIndent(),
                        modifier = Modifier.padding(10.dp),
                    )
                }
            },
            state = rememberBasicTooltipState(),
        ) {
            Icon(Icons.Default.Info, null)
        }

    }
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
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text(if (isInstalled) "Re-download MangaJaNai preset" else "Download MangaJaNai preset")
            }

            ElevatedButton(
                onClick = { uriHandler.openUri("https://github.com/the-database/mangajanai") },
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier.cursorForHand()
            ) {
                Text("Project on Github")
            }
        }
    }
}
