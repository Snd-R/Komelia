package io.github.snd_r.komelia.ui.settings.appearance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.platform.cursorForHand
import kotlin.math.roundToInt

@Composable
fun AppearanceContent(
    cardWidth: Dp,
    onCardWidthChange: (Dp) -> Unit
) {

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Image Card Size")
        Slider(
            value = cardWidth.value,
            onValueChange = { onCardWidthChange(it.roundToInt().dp) },
            steps = 10,
            valueRange = 180f..400f,
            modifier = Modifier.cursorForHand().padding(end = 20.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("${cardWidth.value}")

            Card(
                Modifier
                    .width(cardWidth)
                    .aspectRatio(0.703f)
            ) {

            }



        }
        HorizontalDivider()
        Text("Colors")
    }

}