package snd.komelia.ui.settings.imagereader.onnxruntime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_download
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_load_failed
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_panel_detection
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_update
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_update_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_upscale_settings
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import snd.komelia.image.UpscaleMode
import snd.komelia.onnxruntime.DeviceInfo
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CUDA
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.DirectML
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.ROCm
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.TENSOR_RT
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.WEBGPU
import snd.komelia.ui.LocalPlatform
import snd.komelia.ui.platform.PlatformType
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.updates.UpdateProgress

@Composable
fun OnnxRuntimeSettingsContent(
    executionProvider: OnnxRuntimeExecutionProvider,
    availableDevices: List<DeviceInfo>,
    deviceId: Int,
    onDeviceIdChange: (Int) -> Unit,

    upscaleMode: UpscaleMode,
    onUpscaleModeChange: (UpscaleMode) -> Unit,
    upscalerTileSize: Int,
    onUpscalerTileSizeChange: (Int) -> Unit,
    upscaleModelPath: PlatformFile?,
    onUpscaleModelPathChange: (PlatformFile?) -> Unit,

    onOrtInstall: (provider: OnnxRuntimeExecutionProvider) -> Flow<UpdateProgress>,
    mangaJaNaiIsInstalled: Boolean,
    onMangaJaNaiDownload: () -> Flow<UpdateProgress>,
    panelModelIsDownloaded: Boolean,
    onPanelDetectionModelDownloadRequest: () -> Flow<UpdateProgress>
) {
    val loadError = remember { onnxRuntimeLoadError() }

    var showOrtInstallDialog by remember { mutableStateOf(false) }
    OrtInstallDialog(
        show = showOrtInstallDialog,
        onInstallRequest = { onOrtInstall(it) },
        onDismiss = { showOrtInstallDialog = false }
    )
    val ortExecutionProvider = remember {
        when (executionProvider) {
            TENSOR_RT -> "TensorRT"
            CUDA -> "Cuda"
            ROCm -> "ROCm"
            DirectML -> "DirectML"
            CPU -> "CPU"
            WEBGPU -> "WebGPU"
        }
    }
    val platform = LocalPlatform.current


    val onnxruntimeString = stringResource(Res.string.settings_image_onnxruntime)
    if (!isOnnxRuntimeInstalled() || loadError != null) {
        if (platform == PlatformType.DESKTOP) {
            Text(onnxruntimeString, style = MaterialTheme.typography.titleLarge)
            FilledTonalButton(
                onClick = { showOrtInstallDialog = true },
            ) { Text(stringResource(Res.string.settings_image_onnxruntime_download)) }

            if (loadError != null)
                Text(
                    stringResource(Res.string.settings_image_onnxruntime_load_failed, loadError),
                    style = MaterialTheme.typography.bodySmall
                )
        }
    } else {
        Text("$onnxruntimeString $ortExecutionProvider", style = MaterialTheme.typography.titleLarge)
        if (platform == PlatformType.DESKTOP) {
            DeviceSelector(
                availableDevices = availableDevices,
                executionProvider = executionProvider,
                currentDeviceId = deviceId,
                onDeviceIdChange = onDeviceIdChange
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { showOrtInstallDialog = true },
                    modifier = Modifier.cursorForHand()
                ) { Text(stringResource(Res.string.settings_image_onnxruntime_update), maxLines = 1) }

                Text(
                    stringResource(Res.string.settings_image_onnxruntime_update_desc),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Text(
                stringResource(Res.string.settings_image_onnxruntime_upscale_settings),
                style = MaterialTheme.typography.titleMedium
            )
            UpscalerSettings(
                upscaleMode = upscaleMode,
                onModeChange = onUpscaleModeChange,
                tileSize = upscalerTileSize,
                onTileSizeChange = onUpscalerTileSizeChange,
                userModelPath = upscaleModelPath,
                onModelPathChange = onUpscaleModelPathChange,
                isMangaJaNaiDownloaded = mangaJaNaiIsInstalled,
                onMangaJaNaiDownload = onMangaJaNaiDownload
            )
            HorizontalDivider()
        }
        Text(
            stringResource(Res.string.settings_image_onnxruntime_panel_detection),
            style = MaterialTheme.typography.titleMedium
        )
        PanelDetectionSettings(
            isDownloaded = panelModelIsDownloaded,
            onDownloadRequest = onPanelDetectionModelDownloadRequest
        )
    }
}

