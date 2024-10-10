package io.github.snd_r.komelia.ui.settings.decoder

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.OnnxRuntimeSharedLibraries
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.CPU
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.CUDA
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.DirectML
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.ROCm
import io.github.snd_r.OnnxRuntimeSharedLibraries.OnnxRuntimeExecutionProvider.TENSOR_RT
import io.github.snd_r.OnnxRuntimeUpscaler
import io.github.snd_r.komelia.DesktopPlatform
import io.github.snd_r.komelia.DesktopPlatform.Linux
import io.github.snd_r.komelia.DesktopPlatform.Windows
import io.github.snd_r.komelia.platform.DownscaleOption
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.platform.formatDecimal
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.LabeledEntry.Companion.intEntry
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.updates.UpdateProgress
import io.github.vinceglb.filekit.compose.rememberDirectoryPickerLauncher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

@Composable
fun DecoderSettingsContent(
    decoderDescriptor: PlatformDecoderDescriptor,
    upscaleOption: UpscaleOption,
    onUpscaleOptionChange: (UpscaleOption) -> Unit,
    downscaleOption: DownscaleOption,
    onDownscaleOptionChange: (DownscaleOption) -> Unit,
    gpuInfo: List<OnnxRuntimeUpscaler.DeviceInfo>,
    tileSize: Int,
    onTileSizeChange: (Int) -> Unit,
    deviceId: Int,
    onDeviceIdChange: (Int) -> Unit,

    onnxPath: String,
    onOnnxPathChange: (String) -> Unit,
    onOrtProviderInstall: suspend (OnnxRuntimeExecutionProvider) -> Unit,
    onOrtProviderInstallCancel: () -> Unit,
    ortInstallProgress: UpdateProgress?,
    ortInstallError: String?,
    onOrtInstallErrorDismiss: () -> Unit,

    mangaJaNaiIsDownloaded: Boolean,
    onMangaJaNaiDownload: () -> Flow<UpdateProgress>,

    onCacheClear: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (decoderDescriptor.downscaleOptions.size > 1) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(downscaleOption, downscaleOption.value),
                options = remember(decoderDescriptor) {
                    decoderDescriptor.downscaleOptions.map { LabeledEntry(it, it.value) }
                },
                onOptionChange = { onDownscaleOptionChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text("Downscale method") }
            )
        } else {
            Text("Downscale method: ${downscaleOption.value}")
        }

        if (decoderDescriptor.upscaleOptions.size > 1) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(upscaleOption, upscaleOption.value),
                options = remember(decoderDescriptor) {
                    decoderDescriptor.upscaleOptions.map { LabeledEntry(it, it.value) }
                },
                onOptionChange = { onUpscaleOptionChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text("Upscale method") }
            )
        } else {
            Text("Upscale method: ${upscaleOption.value}")
        }

        if (DesktopPlatform.Current == Linux || DesktopPlatform.Current == Windows) {
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            OnnxRuntimeContent(
                onnxPath = onnxPath,
                onOnnxPathChange = onOnnxPathChange,
                gpuInfo = gpuInfo,
                tileSize = tileSize,
                onTileSizeChange = onTileSizeChange,
                deviceId = deviceId,
                onDeviceIdChange = onDeviceIdChange,

                onOrtProviderInstall = onOrtProviderInstall,
                onOrtProviderInstallCancel = onOrtProviderInstallCancel,
                ortInstallProgress = ortInstallProgress,
                ortInstallError = ortInstallError,
                onOrtInstallErrorDismiss = onOrtInstallErrorDismiss,

                mangaJaNaiIsDownloaded = mangaJaNaiIsDownloaded,
                onMangaJaNaiDownload = onMangaJaNaiDownload
            )
        }

        HorizontalDivider()
        Column {
            Text("Image cache", style = MaterialTheme.typography.titleLarge)
            FilledTonalButton(
                onClick = onCacheClear,
                shape = RoundedCornerShape(5.dp)
            ) { Text("Clear image cache") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun OnnxRuntimeContent(
    onnxPath: String,
    onOnnxPathChange: (String) -> Unit,
    gpuInfo: List<OnnxRuntimeUpscaler.DeviceInfo>,
    tileSize: Int,
    onTileSizeChange: (Int) -> Unit,
    deviceId: Int,
    onDeviceIdChange: (Int) -> Unit,

    onOrtProviderInstall: suspend (OnnxRuntimeExecutionProvider) -> Unit,
    onOrtProviderInstallCancel: () -> Unit,
    ortInstallProgress: UpdateProgress?,
    ortInstallError: String?,
    onOrtInstallErrorDismiss: () -> Unit,

    mangaJaNaiIsDownloaded: Boolean,
    onMangaJaNaiDownload: () -> Flow<UpdateProgress>,
) {
    var showOrtInstallDialog by remember { mutableStateOf(false) }
    var showMangaJaNaiDownloadDialog by remember { mutableStateOf(false) }
    var showPostInstallDialog by remember { mutableStateOf(false) }
    if (showOrtInstallDialog) {
        OrtInstallDialog(
            updateProgress = ortInstallProgress,
            onInstallRequest = {
                onOrtProviderInstall(it)
                showOrtInstallDialog = false
                showPostInstallDialog = true
            },
            onDismiss = {
                showOrtInstallDialog = false
                onOrtProviderInstallCancel()
            })
    }
    if (showPostInstallDialog) {
        RestartDialog(error = ortInstallError, onConfirm = {
            showPostInstallDialog = false
            onOrtInstallErrorDismiss()
        })
    }
    if (showMangaJaNaiDownloadDialog) {
        MangaJaNaiDialog(
            downloadFlow = onMangaJaNaiDownload(),
            onDismiss = { showMangaJaNaiDownloadDialog = false }
        )
    }

    if (!OnnxRuntimeSharedLibraries.isAvailable) {
        Text("ONNX runtime settings")
        FilledTonalButton(
            onClick = { showOrtInstallDialog = true },
            shape = RoundedCornerShape(5.dp)
        ) { Text("Download ONNX Runtime") }

        if (OnnxRuntimeSharedLibraries.loadErrorMessage != null)
            Text(
                "Failed to load ONNX Runtime:\n${OnnxRuntimeSharedLibraries.loadErrorMessage}",
                style = MaterialTheme.typography.bodySmall
            )
        return
    }

    val ortExecutionProvider = remember {
        when (OnnxRuntimeSharedLibraries.executionProvider) {
            TENSOR_RT -> "TensorRT"
            CUDA -> "Cuda"
            ROCm -> "ROCm"
            DirectML -> "DirectML"
            CPU -> "CPU"
        }
    }
    Text("ONNX Runtime $ortExecutionProvider execution provider", style = MaterialTheme.typography.titleLarge)

    if (gpuInfo.isNotEmpty() && OnnxRuntimeSharedLibraries.executionProvider != CPU) {
        val selectedDevice = remember(deviceId) { gpuInfo.find { it.id == deviceId } ?: gpuInfo.first() }
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(selectedDevice, "${selectedDevice.name} ${selectedDevice.memoryGb()}GiB"),
            options = remember { gpuInfo.map { LabeledEntry(it, "${it.name} ${it.memoryGb()}GiB") } },
            onOptionChange = { onDeviceIdChange(it.value.id) },
            label = { Text("GPU") },
            inputFieldModifier = Modifier.fillMaxSize()
        )
    }
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
                        modifier = Modifier.padding(10.dp)
                    )
                }
            },
            state = rememberBasicTooltipState(),
        ) {
            Icon(Icons.Default.Info, null)
        }

    }


    val launcher = rememberDirectoryPickerLauncher { directory ->
        directory?.path?.let { onOnnxPathChange(it) }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = onnxPath,
            onValueChange = onOnnxPathChange,
            label = { Text("ONNX models path") },
            modifier = Modifier.weight(7f)
        )

        ElevatedButton(
            onClick = { launcher.launch() },
            modifier = Modifier.padding(horizontal = 10.dp),
            shape = RoundedCornerShape(5.dp)
        ) {
            Text("Browse")
        }
    }

    if (OnnxRuntimeSharedLibraries.loadErrorMessage != null)
        Text(
            "Failed to load ONNX Runtime:\n${OnnxRuntimeSharedLibraries.loadErrorMessage}",
            style = MaterialTheme.typography.bodySmall
        )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilledTonalButton(
            onClick = { showOrtInstallDialog = true },
            shape = RoundedCornerShape(5.dp),
            modifier = Modifier.cursorForHand()
        ) { Text("Update ONNX Runtime", maxLines = 1) }

        Text("Update or download another version of ONNX Runtime", style = MaterialTheme.typography.labelLarge)
    }
    HorizontalDivider()

    val uriHandler = LocalUriHandler.current
    Column {
        Text("MangaJaNai ONNX models preset", style = MaterialTheme.typography.titleLarge)
        Text(
            """
                MangaJaNai is a collection of upscaling models for manga.
                The models are mainly optimized to upscale digital manga images of Japanese or English text with height ranging from around 1200px to 2048px.
            """.trimIndent()
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
                Text(if (mangaJaNaiIsDownloaded) "Re-download MangaJaNai preset" else "Download MangaJaNai preset")
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

@Composable
private fun OrtInstallDialog(
    updateProgress: UpdateProgress?,
    onInstallRequest: suspend (OnnxRuntimeExecutionProvider) -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var provider by remember { mutableStateOf<OnnxRuntimeExecutionProvider?>(null) }
    AppDialog(
        modifier = Modifier.widthIn(max = 840.dp),
        header = {
            Column(modifier = Modifier.padding(10.dp)) {
                if (updateProgress == null)
                    Text("Choose ONNX Runtime version", style = MaterialTheme.typography.titleLarge)
                else
                    Text("Downloading ONNX Runtime", style = MaterialTheme.typography.titleLarge)
                HorizontalDivider(Modifier.padding(top = 10.dp))
            }
        },
        content = {
            if (updateProgress != null) {
                UpdateProgressContent(updateProgress)
            } else {
                OrtDownloadDialogContent(
                    chosenProvider = provider,
                    onProviderChoice = { provider = it },
                )
            }
        },
        controlButtons = {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {

                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand(),
                    content = { Text("Cancel") }
                )

                FilledTonalButton(
                    enabled = provider != null && updateProgress == null,
                    onClick = {
                        provider?.let {
                            coroutineScope.launch { onInstallRequest(it) }
                        }
                    },
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand(),
                ) {
                    Text("Install")
                }
            }
        },
        onDismissRequest = { if (updateProgress == null) onDismiss() }
    )
}

@Composable
private fun OrtDownloadDialogContent(
    chosenProvider: OnnxRuntimeExecutionProvider?,
    onProviderChoice: (OnnxRuntimeExecutionProvider) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier.border(Dp.Hairline, MaterialTheme.colorScheme.tertiary)
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            Text(
                "ONNX Runtime support is experimental.\nMight cause crashes or use incompatible libraries",
                color = MaterialTheme.colorScheme.tertiaryContainer
            )
        }
        val uriHandler = LocalUriHandler.current
        CheckboxWithLabel(
            checked = chosenProvider == CUDA,
            onCheckedChange = { onProviderChoice(CUDA) },
            labelAlignment = Alignment.Top,
            label = {
                Column {
                    Text("Cuda (Nvidia GPUs, requires CUDA12 and cuDNN9 system install)")
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "download CUDA12",
                            color = MaterialTheme.colorScheme.secondary,
                            textDecoration = Underline,
                            modifier = Modifier
                                .clickable { uriHandler.openUri("https://developer.nvidia.com/cuda-downloads") }
                                .cursorForHand()
                                .padding(horizontal = 5.dp),
                        )
                        Text(
                            "download cuDNN9",
                            color = MaterialTheme.colorScheme.secondary,
                            textDecoration = Underline,
                            modifier = Modifier
                                .clickable { uriHandler.openUri("https://developer.nvidia.com/cudnn-downloads") }
                                .cursorForHand()
                                .padding(horizontal = 5.dp),
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        CheckboxWithLabel(
            checked = chosenProvider == TENSOR_RT,
            onCheckedChange = { onProviderChoice(TENSOR_RT) },
            labelAlignment = Alignment.Top,
            label = {
                Column {
                    Text("TensorRT (Nvidia GPUs, requires CUDA12, cuDNN9 and TensorRT system install)")
                    Text(
                        "Uses TensorRT to create optimized graph engine. Takes a significant time on model first load. After initial load engine is cached for future use",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "download CUDA12",
                            color = MaterialTheme.colorScheme.secondary,
                            textDecoration = Underline,
                            modifier = Modifier
                                .clickable { uriHandler.openUri("https://developer.nvidia.com/cuda-downloads") }
                                .cursorForHand()
                                .padding(horizontal = 5.dp),
                        )
                        Text(
                            "download cuDNN9",
                            color = MaterialTheme.colorScheme.secondary,
                            textDecoration = Underline,
                            modifier = Modifier
                                .clickable { uriHandler.openUri("https://developer.nvidia.com/cudnn-downloads") }
                                .cursorForHand()
                                .padding(horizontal = 5.dp),
                        )
                        Text(
                            "download TensorRT",
                            color = MaterialTheme.colorScheme.secondary,
                            textDecoration = Underline,
                            modifier = Modifier
                                .clickable { uriHandler.openUri("https://developer.nvidia.com/tensorrt") }
                                .cursorForHand()
                                .padding(horizontal = 5.dp),
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

//        if (DesktopPlatform.Current == Linux)
//            CheckboxWithLabel(
//                checked = chosenProvider == ROCm,
//                onCheckedChange = { onProviderChoice(ROCm) },
//                label = { Text("ROCm (AMD GPUs, requires ROCm5 system install)") },
//                modifier = Modifier.fillMaxWidth()
//            )

        if (DesktopPlatform.Current == Windows)
            CheckboxWithLabel(
                checked = chosenProvider == DirectML,
                onCheckedChange = { onProviderChoice(DirectML) },
                label = { Text("DirectML (all GPUs)") },
                modifier = Modifier.fillMaxWidth()
            )

        if (DesktopPlatform.Current == Linux)
            CheckboxWithLabel(
                checked = chosenProvider == CPU,
                onCheckedChange = { onProviderChoice(CPU) },
                label = { Text("CPU") },
                modifier = Modifier.fillMaxWidth()
            )
    }
}

@Composable
private fun UpdateProgressContent(
    progress: UpdateProgress
) {
    Column(
        Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (progress.total == 0L)
            LinearProgressIndicator(modifier = Modifier.fillMaxSize())
        else {
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
            Text("${completedMb}mb / ${totalMb}MiB")
        }

    }
}

@Composable
private fun RestartDialog(
    error: String?,
    onConfirm: () -> Unit,
) {
    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        content = {
            Box(Modifier.padding(30.dp)) {
                if (error != null)
                    Text("Error occurred during installation:\n$error")
                else
                    Text("App restart is required for changes to take effect")
            }
        },
        controlButtons = {
            Box(modifier = Modifier.padding(bottom = 10.dp, end = 10.dp)) {
                FilledTonalButton(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand(),
                ) {
                    Text("Confirm")
                }
            }
        },
        onDismissRequest = { }
    )
}

@Composable
private fun MangaJaNaiDialog(
    downloadFlow: Flow<UpdateProgress>,
    onDismiss: () -> Unit,
) {
    var progress by remember { mutableStateOf(UpdateProgress(0, 0)) }
    LaunchedEffect(Unit) {
        downloadFlow.conflate().collect {
            progress = it
            delay(100)
        }
        onDismiss()
    }

    AppDialog(
        modifier = Modifier.widthIn(max = 600.dp),
        header = {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Downloading MangaJaNai ONNX models", style = MaterialTheme.typography.titleLarge)
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

private fun OnnxRuntimeUpscaler.DeviceInfo.memoryGb(): String {
    return (memory / 1024.0f / 1024.0f / 1024.0f).formatDecimal(2)
}
