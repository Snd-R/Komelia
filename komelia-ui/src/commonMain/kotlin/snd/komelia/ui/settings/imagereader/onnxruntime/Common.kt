package snd.komelia.ui.settings.imagereader.onnxruntime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_gpu
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_upscale_mode_none
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_upscale_model_path
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_upscale_model_path_browse
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_upscale_tiling_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_upscale_tiling_none
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_image_onnxruntime_upscale_tiling_size
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import snd.komelia.formatDecimal
import snd.komelia.image.UpscaleMode
import snd.komelia.onnxruntime.DeviceInfo
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider
import snd.komelia.onnxruntime.OnnxRuntimeExecutionProvider.CPU
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.common.components.LabeledEntry.Companion.intEntry
import snd.komelia.ui.strings.AppStrings
import snd.komelia.ui.strings.stringLabels

@Composable
fun UpscaleModeSelector(
    currentMode: UpscaleMode,
    onModeChange: (UpscaleMode) -> Unit,
    currentModelPath: PlatformFile?,
    onModelPathChange: (PlatformFile?) -> Unit,
) {
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(
            currentMode,
            stringResource(AppStrings.forOnnxRuntimeUpscaleMode(currentMode))

        ),
        options = stringLabels(UpscaleMode.entries) { AppStrings.forOnnxRuntimeUpscaleMode(it) },
        onOptionChange = { onModeChange(it.value) },
        label = { Text(stringResource(Res.string.settings_image_onnxruntime_upscale_mode_none)) },
        inputFieldModifier = Modifier.fillMaxSize()
    )
    AnimatedVisibility(currentMode == UpscaleMode.USER_SPECIFIED_MODEL) {
        val launcher = rememberFilePickerLauncher(
            type = FileKitType.File(listOf("onnx")),
            directory = currentModelPath,
        ) { file -> file?.let { onModelPathChange(it) } }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 10.dp)
        ) {
            TextField(
                value = currentModelPath?.toString() ?: "",
                onValueChange = {},
                enabled = false,
                label = { Text(stringResource(Res.string.settings_image_onnxruntime_upscale_model_path)) },
                readOnly = true,
                modifier = Modifier.weight(7f),
            )

            ElevatedButton(
                onClick = { launcher.launch() },
                modifier = Modifier.padding(horizontal = 10.dp),
            ) {
                Text(stringResource(Res.string.settings_image_onnxruntime_upscale_model_path_browse))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TileSizeSelector(
    tileSize: Int,
    onTileSizeChange: (Int) -> Unit,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val tilingOptionNone = stringResource(Res.string.settings_image_onnxruntime_upscale_tiling_none)
        DropdownChoiceMenu(
            selectedOption = remember(tileSize) {
                if (tileSize == 0) LabeledEntry(0, tilingOptionNone) else intEntry(
                    tileSize
                )
            },
            options = remember {
                listOf(
                    LabeledEntry(0, tilingOptionNone),
                    intEntry(4096),
                    intEntry(2048),
                    intEntry(1024),
                    intEntry(512),
                    intEntry(256),
                    intEntry(128)
                )
            },
            onOptionChange = { onTileSizeChange(it.value) },
            label = { Text(stringResource(Res.string.settings_image_onnxruntime_upscale_tiling_size)) },
            modifier = Modifier.weight(1f),
            inputFieldModifier = Modifier.fillMaxSize()
        )

        BasicTooltipBox(
            positionProvider = rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                Card(
                    modifier = Modifier.widthIn(max = 450.dp),
                    border = BorderStroke(Dp.Hairline, MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = stringResource(Res.string.settings_image_onnxruntime_upscale_tiling_desc),
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
fun DeviceSelector(
    availableDevices: List<DeviceInfo>,
    executionProvider: OnnxRuntimeExecutionProvider,
    currentDeviceId: Int,
    onDeviceIdChange: (Int) -> Unit,
) {
    if (availableDevices.isNotEmpty() && executionProvider != CPU) {
        val selectedDevice = remember(currentDeviceId) {
            availableDevices.find { it.id == currentDeviceId } ?: availableDevices.first()
        }
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(selectedDevice, "${selectedDevice.name} ${selectedDevice.memoryGb()}GiB"),
            options = remember { availableDevices.map { LabeledEntry(it, "${it.name} ${it.memoryGb()}GiB") } },
            onOptionChange = { onDeviceIdChange(it.value.id) },
            label = { Text(stringResource(Res.string.settings_image_onnxruntime_gpu)) },
            inputFieldModifier = Modifier.fillMaxSize()
        )
    }
}


internal fun DeviceInfo.memoryGb(): String {
    return (memory / 1024.0f / 1024.0f / 1024.0f).formatDecimal(2)
}
