package io.github.snd_r.komelia.ui.reader.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.PlatformType
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalPlatform
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.common.SwitchWithLabel
import io.github.snd_r.komelia.ui.reader.ReaderState
import io.github.snd_r.komelia.ui.reader.ReaderType
import io.github.snd_r.komelia.ui.reader.ScreenScaleState
import snd.komga.client.book.KomgaBook
import kotlin.math.roundToInt

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    book: KomgaBook?,
    show: Boolean,

    settingsState: ReaderState,
    screenScaleState: ScreenScaleState,

    onMenuDismiss: () -> Unit,
    onShowHelpMenu: () -> Unit,
    onSeriesPress: () -> Unit,
    onBookClick: () -> Unit,
    readerSettingsContent: @Composable ColumnScope.() -> Unit,
) {
    if (!show) return
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {}
                .width(350.dp)
                .padding(10.dp)
                .imePadding()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.systemBars))

            SettingsContent(
                book = book,
                settingsState = settingsState,
                screenScaleState = screenScaleState,
                onMenuDismiss = onMenuDismiss,
                onShowHelpMenu = onShowHelpMenu,
                onSeriesPress = onSeriesPress,
                onBookClick = onBookClick,
                readerSettingsContent = readerSettingsContent
            )

            Spacer(Modifier.height(30.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.systemBars))
        }
    }
}

@Composable
private fun ColumnScope.SettingsContent(
    book: KomgaBook?,
    settingsState: ReaderState,
    screenScaleState: ScreenScaleState,

    onMenuDismiss: () -> Unit,
    onShowHelpMenu: () -> Unit,
    onSeriesPress: () -> Unit,
    onBookClick: () -> Unit,
    readerSettingsContent: @Composable ColumnScope.() -> Unit,
) {
    Row {
        IconButton(onClick = { onMenuDismiss() }) { Icon(Icons.Default.Close, null) }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { onShowHelpMenu() }) { Icon(Icons.AutoMirrored.Default.Help, null) }
    }
    if (book != null) {
        ReturnLink(Icons.AutoMirrored.Default.MenuBook, book.seriesTitle, onSeriesPress)
        ReturnLink(Icons.Default.Book, book.metadata.title, onBookClick)
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

    val strings = LocalStrings.current.reader

    val readerType = settingsState.readerType.collectAsState().value
    val zoom = screenScaleState.zoom.collectAsState().value
    val zoomPercentage = (zoom * 100).roundToInt()
    Text("${strings.zoom}: $zoomPercentage%")
    DropdownChoiceMenu(
        selectedOption = LabeledEntry(readerType, strings.forReaderType(readerType)),
        options = remember { ReaderType.entries.map { LabeledEntry(it, strings.forReaderType(it)) } },
        onOptionChange = { settingsState.onReaderTypeChange(it.value) },
        inputFieldModifier = Modifier.fillMaxWidth(),
        label = { Text(strings.readerType) },
        inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
    )
    val decoder = settingsState.decoderSettings.collectAsState().value
    val decoderDescriptor = settingsState.currentDecoderDescriptor.collectAsState().value

    if (decoder != null && decoderDescriptor != null && decoderDescriptor.upscaleOptions.size > 1) {
        DropdownChoiceMenu(
            selectedOption = LabeledEntry(decoder.upscaleOption, decoder.upscaleOption.value),
            options = remember { decoderDescriptor.upscaleOptions.map { LabeledEntry(it, it.value) } },
            onOptionChange = { settingsState.onUpscaleMethodChange(it.value) },
            inputFieldModifier = Modifier.fillMaxWidth(),
            label = { Text("Upscale method") },
            inputFieldColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    SwitchWithLabel(
        settingsState.imageStretchToFit.collectAsState().value,
        settingsState::onStretchToFitChange,
        label = { Text(strings.stretchToFit) },
    )

    if (LocalPlatform.current != PlatformType.WEB_KOMF) {
        SwitchWithLabel(
            settingsState.cropBorders.collectAsState().value,
            settingsState::onTrimEdgesChange,
            label = { Text("Crop borders") },
        )
    }

    HorizontalDivider(Modifier.padding(vertical = 5.dp))
    readerSettingsContent()

}

@Composable
private fun ReturnLink(icon: ImageVector, text: String, onClick: () -> Unit) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .cursorForHand(),
    ) {

        Icon(
            icon, null,
            modifier = Modifier.size(35.dp).padding(end = 10.dp)
        )

        Text(
            text = text,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

