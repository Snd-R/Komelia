package io.github.snd_r.komelia.ui.settings.app

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.platform.SamplerType
import io.github.snd_r.komelia.platform.cursorForHand
import io.github.snd_r.komelia.ui.LocalStrings
import io.github.snd_r.komelia.ui.common.DropdownChoiceMenu
import io.github.snd_r.komelia.ui.common.LabeledEntry
import kotlin.math.roundToInt

@Composable
fun AppSettingsContent(
    cardWidth: Dp,
    onCardWidthChange: (Dp) -> Unit,
    decoder: SamplerType?,
    onDecoderTypeChange: (SamplerType) -> Unit,
) {
    val strings = LocalStrings.current.settings

    var showCardSettings by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.animateContentSize(spring(stiffness = Spring.StiffnessLow))
    ) {
        Row(
            modifier = Modifier
                .clickable { showCardSettings = !showCardSettings }
                .fillMaxWidth()
                .cursorForHand(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(strings.imageCardSize, modifier = Modifier.padding(10.dp))
            Spacer(Modifier.width(20.dp))
            Icon(if (showCardSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
        }
        if (showCardSettings) {
            Slider(
                value = cardWidth.value,
                onValueChange = { onCardWidthChange(it.roundToInt().dp) },
                steps = 19,
                valueRange = 150f..350f,
                modifier = Modifier.cursorForHand().padding(end = 20.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp, max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text("${cardWidth.value}")

                Card(
                    Modifier
                        .width(cardWidth)
                        .aspectRatio(0.703f)
                ) {

                }


            }
        }
        HorizontalDivider(Modifier.padding(end = 20.dp))

        if (decoder != null) {
            DropdownChoiceMenu(
                selectedOption = LabeledEntry(decoder, decoder.name),
                options = SamplerType.entries.map { LabeledEntry(it, it.name) },
                onOptionChange = { onDecoderTypeChange(it.value) },
                textFieldModifier = Modifier.fillMaxWidth(),
                label = { Text(strings.decoder) }
            )
        }
    }

}