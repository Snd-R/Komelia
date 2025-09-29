package io.github.snd_r.komelia.ui.settings.imagereader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.settings.imagereader.onnxruntime.OnnxRuntimeSettingsContent
import io.github.snd_r.komelia.ui.settings.imagereader.onnxruntime.OnnxRuntimeSettingsState
import io.github.snd_r.komelia.ui.settings.imagereader.onnxruntime.isOnnxRuntimeSupported

@Composable
fun ImageReaderSettingsContent(
    loadThumbnailPreviews: Boolean,
    onLoadThumbnailPreviewsChange: (Boolean) -> Unit,

    volumeKeysNavigation: Boolean,
    onVolumeKeysNavigationChange: (Boolean) -> Unit,

    onCacheClear: () -> Unit,
    onnxRuntimeSettingsState: OnnxRuntimeSettingsState,
) {
    val strings = LocalStrings.current.imageSettings
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        val platform = LocalPlatform.current
        SwitchWithLabel(
            checked = loadThumbnailPreviews,
            onCheckedChange = onLoadThumbnailPreviewsChange,
            label = { Text("Load small previews when dragging navigation slider") },
            supportingText = { Text("can be slow for high resolution images") },
        )

        if (platform == PlatformType.MOBILE) {
            SwitchWithLabel(
                checked = volumeKeysNavigation,
                onCheckedChange = onVolumeKeysNavigationChange,
                label = { Text("Volume keys navigation") },
            )
        }

        FilledTonalButton(
            onClick = onCacheClear,
            shape = RoundedCornerShape(5.dp)
        ) { Text("Clear image cache") }

        if (isOnnxRuntimeSupported()) {
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            OnnxRuntimeSettingsContent(
                executionProvider = onnxRuntimeSettingsState.currentExecutionProvider,
                availableDevices = onnxRuntimeSettingsState.availableDevices,
                deviceId = onnxRuntimeSettingsState.deviceId.collectAsState().value,
                onDeviceIdChange = onnxRuntimeSettingsState::onDeviceIdChange,
                upscaleMode = onnxRuntimeSettingsState.upscaleMode.collectAsState().value,
                onUpscaleModeChange = onnxRuntimeSettingsState::onUpscaleModeChange,
                upscalerTileSize = onnxRuntimeSettingsState.upscalerTileSize.collectAsState().value,
                onUpscalerTileSizeChange = onnxRuntimeSettingsState::onTileSizeChange,
                upscaleModelPath = onnxRuntimeSettingsState.upscaleModelPath.collectAsState().value,
                onUpscaleModelPathChange = onnxRuntimeSettingsState::onUpscaleModelPathChange,
                onOrtInstall = onnxRuntimeSettingsState::onInstallRequest,
                mangaJaNaiIsInstalled = onnxRuntimeSettingsState.mangaJaNaiIsInstalled.collectAsState().value,
                onMangaJaNaiDownload = onnxRuntimeSettingsState::onMangaJaNaiDownloadRequest,
                panelModelIsDownloaded = onnxRuntimeSettingsState.panelModelIsDownloaded.collectAsState().value,
                onPanelDetectionModelDownloadRequest = onnxRuntimeSettingsState::onPanelDetectionModelDownloadRequest

            )
        }
    }
}

