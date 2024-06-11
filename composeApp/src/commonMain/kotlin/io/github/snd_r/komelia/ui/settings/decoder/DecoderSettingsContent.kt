package io.github.snd_r.komelia.ui.settings.decoder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import io.github.snd_r.komelia.platform.DownscaleOption
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.PlatformDecoderType
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry

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
            Text("Decoder: ${decoder.getDisplayName()}")
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

        val platform = LocalPlatform.current
        if (platform != PlatformType.DESKTOP) return

        if (decoderDescriptor.isOnnx) {

            HorizontalDivider()

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
    }

}