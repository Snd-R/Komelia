package snd.komelia.ui.color.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import snd.komelia.color.ColorChannel
import snd.komelia.color.ColorChannel.BLUE
import snd.komelia.color.ColorChannel.GREEN
import snd.komelia.color.ColorChannel.RED
import snd.komelia.color.ColorChannel.VALUE
import snd.komelia.color.HistogramPaths

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tooltip(text: String, content: @Composable () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above, 5.dp),
        tooltip = { PlainTooltip { Text(text) } },
        state = rememberTooltipState(),
        content = content
    )

}

fun histogramDrawOrder(
    selectedChannel: ColorChannel,
    histogramPathData: HistogramPaths,
): List<Pair<Path, Color>> {
    val redColor = Color(150, 15, 15, 86)
    val greenColor = Color(15, 150, 15, 86)
    val blueColor = Color(15, 15, 150, 86)
    val valueColor = Color(100, 100, 100, 86)

    return when (selectedChannel) {
        VALUE -> listOfNotNull(
            histogramPathData.red?.let { it to redColor },
            histogramPathData.green?.let { it to greenColor },
            histogramPathData.blue?.let { it to blueColor },
            histogramPathData.color?.let { it to valueColor.copy(alpha = 0.7f) },
        )

        RED -> listOfNotNull(
            histogramPathData.color?.let { it to valueColor },
            histogramPathData.green?.let { it to greenColor },
            histogramPathData.blue?.let { it to blueColor },
            histogramPathData.red?.let { it to redColor.copy(alpha = 0.7f) },
        )

        GREEN -> listOfNotNull(
            histogramPathData.color?.let { it to valueColor },
            histogramPathData.red?.let { it to redColor },
            histogramPathData.blue?.let { it to blueColor },
            histogramPathData.green?.let { it to greenColor.copy(alpha = 0.7f) },
        )

        BLUE -> listOfNotNull(
            histogramPathData.color?.let { it to valueColor },
            histogramPathData.red?.let { it to redColor },
            histogramPathData.green?.let { it to greenColor },
            histogramPathData.blue?.let { it to blueColor.copy(alpha = 0.6f) },
        )
    }
}
