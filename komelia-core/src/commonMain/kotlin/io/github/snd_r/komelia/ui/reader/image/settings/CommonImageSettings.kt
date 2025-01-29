package io.github.snd_r.komelia.ui.reader.image.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.AppSliderDefaults
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.reader.image.ReaderFlashColor
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommonImageSettings(
    decoder: PlatformDecoderSettings?,
    decoderDescriptor: PlatformDecoderDescriptor?,
    onUpscaleMethodChange: (UpscaleOption) -> Unit,

    stretchToFit: Boolean,
    onStretchToFitChange: (Boolean) -> Unit,
    cropBorders: Boolean,
    onCropBordersChange: (Boolean) -> Unit,

    isColorCorrectionsActive: Boolean,
    onColorCorrectionClick: () -> Unit,

    flashEnabled: Boolean,
    onFlashEnabledChange: (Boolean) -> Unit,
    flashEveryNPages: Int,
    onFlashEveryNPagesChange: (Int) -> Unit,
    flashWith: ReaderFlashColor,
    onFlashWithChange: (ReaderFlashColor) -> Unit,
    flashDuration: Long,
    onFlashDurationChange: (Long) -> Unit,

    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current.reader
    val platform = LocalPlatform.current
    Column(modifier = modifier) {
        if (decoder != null && decoderDescriptor != null && decoderDescriptor.upscaleOptions.size > 1) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(decoder.upscaleOption, decoder.upscaleOption.value),
                options = remember { decoderDescriptor.upscaleOptions.map { LabeledEntry(it, it.value) } },
                onOptionChange = { onUpscaleMethodChange(it.value) },
                inputFieldModifier = Modifier.fillMaxWidth(),
                label = { Text("Upscale method") },
                inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        Row(
            modifier = Modifier
                .clickable { onColorCorrectionClick() }
                .pointerHoverIcon(PointerIcon.Hand)
                .padding(horizontal = 10.dp, vertical = 15.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Color Correction")
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = if (isColorCorrectionsActive) MaterialTheme.colorScheme.secondary
                else LocalContentColor.current
            )
            if (isColorCorrectionsActive) {
                Text(
                    "active",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        SwitchWithLabel(
            checked = stretchToFit,
            onCheckedChange = onStretchToFitChange,
            label = { Text(strings.stretchToFit) },
            contentPadding = PaddingValues(horizontal = 10.dp)
        )

        if (LocalPlatform.current != PlatformType.WEB_KOMF) {
            SwitchWithLabel(
                checked = cropBorders,
                onCheckedChange = onCropBordersChange,
                label = { Text("Crop borders") },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )
        }

        if (platform != PlatformType.DESKTOP) {
            SwitchWithLabel(
                checked = flashEnabled,
                onCheckedChange = onFlashEnabledChange,
                label = { Text("Flash on page change") },
                supportingText = { Text("Prevents ghosting on e-ink devices") },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )
            AnimatedVisibility(flashEnabled) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.width(100.dp)) {
                            Text("Flash Duration", style = MaterialTheme.typography.labelLarge)
                            Text("$flashDuration ms", style = MaterialTheme.typography.labelMedium)
                        }
                        Slider(
                            value = flashDuration.toFloat(),
                            onValueChange = { onFlashDurationChange(it.roundToLong()) },
                            steps = 13,
                            valueRange = 100f..1500f,
                            colors = AppSliderDefaults.colors()
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.width(100.dp)) {
                            Text("Flash every", style = MaterialTheme.typography.labelLarge)
                            val pagesText = remember(flashEveryNPages) {
                                if (flashEveryNPages == 1) "$flashEveryNPages page"
                                else "$flashEveryNPages pages"
                            }
                            Text(pagesText, style = MaterialTheme.typography.labelMedium)
                        }
                        Slider(
                            value = flashEveryNPages.toFloat(),
                            onValueChange = { onFlashEveryNPagesChange(it.roundToInt()) },
                            steps = 10,
                            valueRange = 1f..10f,
                            colors = AppSliderDefaults.colors()
                        )
                    }

                    Column {
                        Text("Flash with")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            InputChip(
                                selected = flashWith == ReaderFlashColor.BLACK,
                                onClick = { onFlashWithChange(ReaderFlashColor.BLACK) },
                                label = { Text("Black") }
                            )
                            InputChip(
                                selected = flashWith == ReaderFlashColor.WHITE,
                                onClick = { onFlashWithChange(ReaderFlashColor.WHITE) },
                                label = { Text("White") }
                            )
                            InputChip(
                                selected = flashWith == ReaderFlashColor.WHITE_AND_BLACK,
                                onClick = { onFlashWithChange(ReaderFlashColor.WHITE_AND_BLACK) },
                                label = { Text("White and Black") }
                            )
                        }
                    }
                }
            }
        }
    }
}
