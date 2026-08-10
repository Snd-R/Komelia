package snd.komelia.ui.reader.image.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_color_correction
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_color_correction_active
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_crop_borders
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_flash_duration
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_flash_duration_ms
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_flash_every
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_flash_every_n_pages
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_flash_on_page_change
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_flash_on_page_change_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_flash_with
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_flash_with_black
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_flash_with_white
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_flash_with_white_and_black
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.reader_image_stretch_small_images
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import snd.komelia.settings.model.ReaderFlashColor
import snd.komelia.ui.LocalPlatform
import snd.komelia.ui.common.components.AppSliderDefaults
import snd.komelia.ui.common.components.SwitchWithLabel
import snd.komelia.ui.platform.PlatformType
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
fun CommonImageSettings(
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
    val platform = LocalPlatform.current
    Column(modifier = modifier) {
        SwitchWithLabel(
            checked = stretchToFit,
            onCheckedChange = onStretchToFitChange,
            label = { Text(stringResource(Res.string.reader_image_stretch_small_images)) },
            contentPadding = PaddingValues(horizontal = 10.dp)
        )

        if (LocalPlatform.current != PlatformType.WEB_KOMF) {
            SwitchWithLabel(
                checked = cropBorders,
                onCheckedChange = onCropBordersChange,
                label = { Text(stringResource(Res.string.reader_image_crop_borders)) },
                contentPadding = PaddingValues(horizontal = 10.dp)
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
            Text(stringResource(Res.string.reader_image_color_correction))
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = if (isColorCorrectionsActive) MaterialTheme.colorScheme.secondary
                else LocalContentColor.current
            )
            if (isColorCorrectionsActive) {
                Text(
                    stringResource(Res.string.reader_image_color_correction_active),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }



        if (platform != PlatformType.DESKTOP) {
            SwitchWithLabel(
                checked = flashEnabled,
                onCheckedChange = onFlashEnabledChange,
                label = { Text(stringResource(Res.string.reader_image_flash_on_page_change)) },
                supportingText = { Text(stringResource(Res.string.reader_image_flash_on_page_change_desc)) },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )
            AnimatedVisibility(flashEnabled) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.width(100.dp)) {
                            Text(
                                stringResource(Res.string.reader_image_flash_duration),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                stringResource(Res.string.reader_image_flash_duration_ms, flashDuration),
                                style = MaterialTheme.typography.labelMedium
                            )
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
                            Text(
                                stringResource(Res.string.reader_image_flash_every),
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                pluralStringResource(
                                    Res.plurals.reader_image_flash_every_n_pages, flashEveryNPages, flashEveryNPages
                                ),
                                style = MaterialTheme.typography.labelMedium
                            )
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
                        Text(stringResource(Res.string.reader_image_flash_with))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            InputChip(
                                selected = flashWith == ReaderFlashColor.BLACK,
                                onClick = { onFlashWithChange(ReaderFlashColor.BLACK) },
                                label = { Text(stringResource(Res.string.reader_image_flash_with_black)) }
                            )
                            InputChip(
                                selected = flashWith == ReaderFlashColor.WHITE,
                                onClick = { onFlashWithChange(ReaderFlashColor.WHITE) },
                                label = { Text(stringResource(Res.string.reader_image_flash_with_white)) }
                            )
                            InputChip(
                                selected = flashWith == ReaderFlashColor.WHITE_AND_BLACK,
                                onClick = { onFlashWithChange(ReaderFlashColor.WHITE_AND_BLACK) },
                                label = { Text(stringResource(Res.string.reader_image_flash_with_white_and_black)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
