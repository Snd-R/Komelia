package io.github.snd_r.komelia.ui.reader.image.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformDecoderDescriptor
import io.github.snd_r.komelia.platform.PlatformDecoderSettings
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.UpscaleOption
import io.github.snd_r.komelia.settings.CommonSettingsRepository
import io.github.snd_r.komelia.settings.ImageReaderSettingsRepository
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.dialogs.tabs.DialogTab
import io.github.snd_r.komelia.ui.dialogs.tabs.TabItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ImageSettingsGeneralState(
    private val settingsRepository: CommonSettingsRepository,
    private val readerSettingsRepository: ImageReaderSettingsRepository,
    private val decoderDescriptor: Flow<PlatformDecoderDescriptor>,
    private val coroutineScope: CoroutineScope
) {
    val currentDecoderDescriptor = MutableStateFlow<PlatformDecoderDescriptor?>(null)
    val decoderSettings = MutableStateFlow<PlatformDecoderSettings?>(null)
    val imageStretchToFit = MutableStateFlow(true)
    val cropBorders = MutableStateFlow(false)

    suspend fun initialize() {
        decoderSettings.value = settingsRepository.getDecoderSettings().first()
        imageStretchToFit.value = readerSettingsRepository.getStretchToFit().first()
        cropBorders.value = readerSettingsRepository.getCropBorders().first()

        decoderDescriptor.onEach { currentDecoderDescriptor.value = it }.launchIn(coroutineScope)
    }

    fun onUpscaleMethodChange(upscaleOption: UpscaleOption) {
        val currentDecoder = requireNotNull(this.decoderSettings.value)
        val newDecoder = currentDecoder.copy(upscaleOption = upscaleOption)
        this.decoderSettings.value = newDecoder
        coroutineScope.launch { settingsRepository.putDecoderSettings(newDecoder) }
    }

    fun onStretchToFitChange(stretch: Boolean) {
        imageStretchToFit.value = stretch
        coroutineScope.launch { readerSettingsRepository.putStretchToFit(stretch) }
    }

    fun onTrimEdgesChange(trim: Boolean) {
        cropBorders.value = trim
        coroutineScope.launch { readerSettingsRepository.putCropBorders(trim) }
    }
}

class ImageSettingsGeneralTab(private val state: ImageSettingsGeneralState) : DialogTab {

    override fun options() = TabItem(
        title = "GENERAL",
        icon = Icons.Default.FormatAlignCenter
    )

    @Composable
    override fun Content() {
        val strings = LocalStrings.current.reader
        Column {
            val decoder = state.decoderSettings.collectAsState().value
            val decoderDescriptor = state.currentDecoderDescriptor.collectAsState().value
            SwitchWithLabel(
                checked = state.imageStretchToFit.collectAsState().value,
                onCheckedChange = state::onStretchToFitChange,
                label = { Text(strings.stretchToFit) },
                contentPadding = PaddingValues(horizontal = 10.dp)
            )

            if (LocalPlatform.current != PlatformType.WEB_KOMF) {
                HorizontalDivider()
                SwitchWithLabel(
                    checked = state.cropBorders.collectAsState().value,
                    onCheckedChange = state::onTrimEdgesChange,
                    label = { Text("Crop borders") },
                    contentPadding = PaddingValues(horizontal = 10.dp)
                )
            }
            if (decoder != null && decoderDescriptor != null && decoderDescriptor.upscaleOptions.size > 1) {
                HorizontalDivider()
                DropdownChoiceMenu(
                    selectedOption = LabeledEntry(decoder.upscaleOption, decoder.upscaleOption.value),
                    options = remember { decoderDescriptor.upscaleOptions.map { LabeledEntry(it, it.value) } },
                    onOptionChange = { state.onUpscaleMethodChange(it.value) },
                    inputFieldModifier = Modifier.fillMaxWidth(),
                    label = { Text("Upscale method") },
                    inputFieldColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}
