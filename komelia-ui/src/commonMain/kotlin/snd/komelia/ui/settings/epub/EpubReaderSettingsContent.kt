package snd.komelia.ui.settings.epub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_epub_reader_type
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_epub_reader_type_komga_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_epub_reader_type_ttsu_desc
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.settings_epub_reader_type_ttsu_github
import org.jetbrains.compose.resources.stringResource
import snd.komelia.settings.model.EpubReaderType
import snd.komelia.settings.model.EpubReaderType.KOMGA_EPUB
import snd.komelia.settings.model.EpubReaderType.TTSU_EPUB
import snd.komelia.ui.common.components.DropdownChoiceMenu
import snd.komelia.ui.common.components.LabeledEntry
import snd.komelia.ui.platform.cursorForHand
import snd.komelia.ui.strings.AppStrings
import snd.komelia.ui.strings.stringLabels

@Composable
fun EpubReaderSettingsContent(
    readerType: EpubReaderType,
    onReaderChange: (EpubReaderType) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(
                    readerType,
                    stringResource(AppStrings.forEpubReaderType(readerType))
                ),
                options = stringLabels(EpubReaderType.entries) { AppStrings.forEpubReaderType(it) },
                onOptionChange = { onReaderChange(it.value) },
                label = { Text(stringResource(Res.string.settings_epub_reader_type)) },
                inputFieldModifier = Modifier.fillMaxWidth().animateContentSize(),
                modifier = Modifier.weight(1f),
            )

            AnimatedVisibility(readerType == TTSU_EPUB) {
                val uriHandler = LocalUriHandler.current
                ElevatedButton(
                    onClick = { uriHandler.openUri("https://github.com/ttu-ttu/ebook-reader") },
                    modifier = Modifier.cursorForHand().padding(start = 20.dp)
                ) {
                    Text(stringResource(Res.string.settings_epub_reader_type_ttsu_github))
                }
            }
        }


        when (readerType) {
            TTSU_EPUB -> Text(stringResource(Res.string.settings_epub_reader_type_ttsu_desc))
            KOMGA_EPUB -> Text(stringResource(Res.string.settings_epub_reader_type_komga_desc))
        }
    }
}