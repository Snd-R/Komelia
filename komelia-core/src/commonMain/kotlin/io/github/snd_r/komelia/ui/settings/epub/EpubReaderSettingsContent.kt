package io.github.snd_r.komelia.ui.settings.epub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType.KOMGA_EPUB
import io.github.snd_r.komelia.ui.settings.epub.EpubReaderType.TTSU_EPUB

@Composable
fun EpubReaderSettingsContent(
    readerType: EpubReaderType,
    onReaderChange: (EpubReaderType) -> Unit,
) {
    val strings = LocalStrings.current.settings
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DropdownChoiceMenu(
                selectedOption = remember(readerType) {
                    LabeledEntry(
                        readerType,
                        strings.forEpubReaderType(readerType)
                    )
                },
                options = remember { EpubReaderType.entries.map { LabeledEntry(it, strings.forEpubReaderType(it)) } },
                onOptionChange = { onReaderChange(it.value) },
                label = { Text("Reader Type") },
                inputFieldModifier = Modifier.fillMaxWidth().animateContentSize(),
                modifier = Modifier.weight(1f),
            )

            AnimatedVisibility(readerType == TTSU_EPUB) {
                val uriHandler = LocalUriHandler.current
                ElevatedButton(
                    onClick = { uriHandler.openUri("https://github.com/ttu-ttu/ebook-reader") },
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier.cursorForHand().padding(start = 20.dp)
                ) {
                    Text("Project on Github")
                }
            }
        }


        when (readerType) {
            TTSU_EPUB -> Text(
                """
                    Loads entire book data at once. May cause long load times or performance issues
                    Adapted for use in Komelia with storage/statistics features removed
                """.trimIndent()
            )

            KOMGA_EPUB -> Text("Komga webui epub reader adapted for use in Komelia")

        }
    }
}