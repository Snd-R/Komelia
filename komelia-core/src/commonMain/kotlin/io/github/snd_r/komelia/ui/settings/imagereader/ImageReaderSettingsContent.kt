package io.github.snd_r.komelia.ui.settings.imagereader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.image.UpsamplingMode
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import snd.komelia.image.ReduceKernel

@Composable
fun ImageReaderSettingsContent(
    availableUpsamplingModes: List<UpsamplingMode>,
    upsamplingMode: UpsamplingMode,
    onUpsamplingModeChange: (UpsamplingMode) -> Unit,

    availableDownsamplingKernels: List<ReduceKernel>,
    downsamplingKernel: ReduceKernel,
    onDownsamplingKernelChange: (ReduceKernel) -> Unit,
    downsampleInLinearLight: Boolean,
    onDownsampleInLinearLightChange: (Boolean) -> Unit,

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

        if (availableUpsamplingModes.size > 1) {
            DropdownChoiceMenu(
                selectedOption = remember(upsamplingMode) {
                    LabeledEntry(upsamplingMode, strings.forUpsamplingMode(upsamplingMode))
                },
                options = remember(availableUpsamplingModes) {
                    availableUpsamplingModes.map { LabeledEntry(it, strings.forUpsamplingMode(it)) }
                },
                onOptionChange = { onUpsamplingModeChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text(strings.upsamplingMode) }
            )
        } else {
            Text("${strings.upsamplingMode}: ${strings.forUpsamplingMode(upsamplingMode)}")
        }

        if (availableDownsamplingKernels.size > 1) {
            DropdownChoiceMenu(
                selectedOption = remember(downsamplingKernel) {
                    LabeledEntry(downsamplingKernel, strings.forDownsamplingKernel(downsamplingKernel))
                },
                options = remember(availableDownsamplingKernels) {
                    availableDownsamplingKernels.map { LabeledEntry(it, strings.forDownsamplingKernel(it)) }
                },
                onOptionChange = { onDownsamplingKernelChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text(strings.downsamplingKernel) }
            )
        }
        HorizontalDivider()

        if (platform != PlatformType.WEB_KOMF) {
            SwitchWithLabel(
                checked = downsampleInLinearLight,
                onCheckedChange = onDownsampleInLinearLightChange,
                label = { Text("Downscale images in linear light") },
                supportingText = { Text("slower but potentially more accurate") },
            )
        }

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

        if (isOnnxRuntimeSupported()) {
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            OnnxRuntimeSettings(state = onnxRuntimeSettingsState)
        }

        HorizontalDivider()

        FilledTonalButton(
            onClick = onCacheClear,
            shape = RoundedCornerShape(5.dp)
        ) { Text("Clear image cache") }
    }
}

