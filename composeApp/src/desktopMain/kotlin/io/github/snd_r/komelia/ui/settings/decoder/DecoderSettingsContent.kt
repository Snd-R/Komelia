package io.github.snd_r.komelia.ui.settings.decoder

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import io.github.snd_r.VipsOnnxRuntimeDecoder
import io.github.snd_r.VipsOnnxRuntimeDecoder.OnnxRuntimeExecutionProvider
import io.github.snd_r.komelia.DesktopPlatform
import io.github.snd_r.komelia.DesktopPlatform.Linux
import io.github.snd_r.komelia.DesktopPlatform.Windows
import io.github.snd_r.komelia.platform.DownscaleOption
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.PlatformDecoderType
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.platform.formatDecimal
import io.github.snd_r.komelia.ui.common.CheckboxWithLabel
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.dialogs.AppDialog
import io.github.snd_r.komelia.updates.UpdateProgress
import kotlinx.coroutines.launch

@Composable
fun DecoderSettingsContent(
    availableDecoders: List<PlatformDecoderDescriptor>,
    decoderDescriptor: PlatformDecoderDescriptor,
    decoder: PlatformDecoderType,
    onDecoderChange: (PlatformDecoderType) -> Unit,
    upscaleOption: UpscaleOption,
    onUpscaleOptionChange: (UpscaleOption) -> Unit,
    downscaleOption: DownscaleOption,
    onDownscaleOptionChange: (DownscaleOption) -> Unit,

    onnxPath: String,
    onOnnxPathChange: (String) -> Unit,
    onOrtProviderInstall: suspend (OnnxRuntimeExecutionProvider) -> Unit,
    onOrtProviderInstallCancel: () -> Unit,
    ortInstallProgress: UpdateProgress?,

    ortInstallError: String?,
    onOrtInstallErrorDismiss: () -> Unit,

    onCacheClear: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (availableDecoders.size > 1) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(decoder, decoder.getDisplayName()),
                options = remember {
                    availableDecoders.map { LabeledEntry(it.platformType, it.platformType.getDisplayName()) }
                },
                onOptionChange = { onDecoderChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text("Decoder") }
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Decoder: ${decoder.getDisplayName()}", style = MaterialTheme.typography.titleMedium)

                if (decoder == PlatformDecoderType.VIPS_ONNX) {
                    val ortExecutionProvider = remember {
                        if (VipsOnnxRuntimeDecoder.isCudaAvailable) "Cuda"
                        else if (VipsOnnxRuntimeDecoder.isRocmAvailable) "ROCm"
                        else if (DesktopPlatform.Current == Windows) "DirectML"
                        else "CPU"
                    }
                    Text(
                        "  (ONNX Runtime $ortExecutionProvider execution provider)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

        }

        if (decoderDescriptor.downscaleOptions.size > 1) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(downscaleOption, downscaleOption.value),
                options = remember {
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
                options = remember {
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
                decoder = decoder,
                onnxPath = onnxPath,
                onOnnxPathChange = onOnnxPathChange,
                onOrtProviderInstall = onOrtProviderInstall,
                onOrtProviderInstallCancel = onOrtProviderInstallCancel,
                ortInstallProgress = ortInstallProgress,
                ortInstallError = ortInstallError,
                onOrtInstallErrorDismiss = onOrtInstallErrorDismiss
            )
        }

        HorizontalDivider(Modifier.padding(vertical = 10.dp))

        Text("Image cache")
        FilledTonalButton(
            onClick = onCacheClear,
            shape = RoundedCornerShape(5.dp)
        ) { Text("Clear image cache") }
    }
}

@Composable
private fun OnnxRuntimeContent(
    decoder: PlatformDecoderType,
    onnxPath: String,
    onOnnxPathChange: (String) -> Unit,
    onOrtProviderInstall: suspend (OnnxRuntimeExecutionProvider) -> Unit,
    onOrtProviderInstallCancel: () -> Unit,
    ortInstallProgress: UpdateProgress?,
    ortInstallError: String?,
    onOrtInstallErrorDismiss: () -> Unit,
) {
    var showOrtInstallDialog by remember { mutableStateOf(false) }
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

    Text("ONNX runtime settings")
    if (decoder == PlatformDecoderType.VIPS_ONNX) {
        var showFilePicker by remember { mutableStateOf(false) }
        DirectoryPicker(show = showFilePicker) { path ->
            if (path != null) {
                onOnnxPathChange(path)
            }
            showFilePicker = false
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = onnxPath,
                onValueChange = onOnnxPathChange,
                label = { Text("ONNX models path") },
                modifier = Modifier.weight(7f)
            )

            ElevatedButton(
                onClick = { showFilePicker = true },
                modifier = Modifier.padding(horizontal = 10.dp),
                shape = RoundedCornerShape(5.dp)
            ) {
                Text("Browse")
            }
        }
    }


    if (decoder == PlatformDecoderType.VIPS_ONNX) {
        FilledTonalButton(
            onClick = { showOrtInstallDialog = true },
            shape = RoundedCornerShape(5.dp)
        ) { Text("Update ONNX Runtime") }
    } else {
        FilledTonalButton(
            onClick = { showOrtInstallDialog = true },
            shape = RoundedCornerShape(5.dp)
        ) { Text("Download ONNX Runtime") }

        if (VipsOnnxRuntimeDecoder.loadErrorMessage != null)
            Text(
                "Failed to load ONNX Runtime:\n${VipsOnnxRuntimeDecoder.loadErrorMessage}",
                style = MaterialTheme.typography.bodySmall
            )
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
        modifier = Modifier.widthIn(max = 600.dp),
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
                OrtDownloadProgressDialogContent(updateProgress)
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
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        CheckboxWithLabel(
            checked = chosenProvider == OnnxRuntimeExecutionProvider.CUDA,
            onCheckedChange = { onProviderChoice(OnnxRuntimeExecutionProvider.CUDA) },
            label = { Text("Cuda (Nvida GPUs, requires Cuda12 system install)") }
        )

        if (DesktopPlatform.Current == Linux)
            CheckboxWithLabel(
                checked = chosenProvider == OnnxRuntimeExecutionProvider.ROCm,
                onCheckedChange = { onProviderChoice(OnnxRuntimeExecutionProvider.ROCm) },
                label = { Text("ROCm (AMD GPUs, requires ROCm5 system install)") }
            )

        if (DesktopPlatform.Current == Windows)
            CheckboxWithLabel(
                checked = chosenProvider == OnnxRuntimeExecutionProvider.DirectML,
                onCheckedChange = { onProviderChoice(OnnxRuntimeExecutionProvider.DirectML) },
                label = { Text("DirectML (all GPUs)") }
            )

        if (DesktopPlatform.Current == Linux)
            CheckboxWithLabel(
                checked = chosenProvider == OnnxRuntimeExecutionProvider.CPU,
                onCheckedChange = { onProviderChoice(OnnxRuntimeExecutionProvider.CPU) },
                label = { Text("CPU") }
            )
    }
}

@Composable
private fun OrtDownloadProgressDialogContent(
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
            Text("${completedMb}mb / ${totalMb}mb")
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
                    Text("Restart is required for changes to take effect")
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
