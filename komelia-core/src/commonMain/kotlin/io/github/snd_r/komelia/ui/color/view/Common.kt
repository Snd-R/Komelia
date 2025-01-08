package io.github.snd_r.komelia.ui.color.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.color.ColorChannel
import io.github.snd_r.komelia.color.ColorChannel.BLUE
import io.github.snd_r.komelia.color.ColorChannel.GREEN
import io.github.snd_r.komelia.color.ColorChannel.RED
import io.github.snd_r.komelia.color.ColorChannel.VALUE
import io.github.snd_r.komelia.color.HistogramPaths
import io.github.snd_r.komelia.platform.formatDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
fun NumberFieldWithIncrements(
    value: Float?,
    onvValueChange: (Float) -> Unit,
    label: String,
    stepSize: Float,
    minValue: Float,
    maxValue: Float,
    digitsAfterDecimal: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        var isTextFieldBlank by remember(value) { mutableStateOf(false) }
        val valueString = remember(value) { value?.formatDecimal(digitsAfterDecimal) ?: "" }
        OutlinedTextField(
            value = if (isTextFieldBlank) "" else valueString,
            onValueChange = { newValue ->
                if (newValue.isBlank()) isTextFieldBlank = true
                else {
                    isTextFieldBlank = false
                    val newFloat = newValue.toFloatOrNull() ?: return@OutlinedTextField
                    onvValueChange(newFloat.coerceIn(minValue, maxValue))
                }
            },
            enabled = value != null,
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.widthIn(max = 90.dp)
        )
        Column {
            val ripple = ripple()
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = null,
                modifier = Modifier
                    .size(25.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .indication(remember { MutableInteractionSource() }, ripple)
                    .doWhilePointerPressed { value?.let { onvValueChange((it + stepSize).coerceAtMost(maxValue)) } }
                    .clickable(enabled = value != null) { }
            )
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = null,
                modifier = Modifier
                    .size(25.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .indication(remember { MutableInteractionSource() }, ripple)
                    .doWhilePointerPressed { value?.let { onvValueChange((it - stepSize).coerceAtLeast(minValue)) } }
                    .clickable(enabled = value != null) { }
            )
        }
    }
}

private fun Modifier.doWhilePointerPressed(
    action: () -> Unit,
): Modifier = composed {
    var isPointerPressed by remember { mutableStateOf(false) }
    val currentAction by rememberUpdatedState(action)

    LaunchedEffect(isPointerPressed) {
        if (!isPointerPressed) return@LaunchedEffect
        withContext(Dispatchers.Default) {
            currentAction()
            delay(200)
            while (isActive) {
                currentAction()
                delay(10)
            }
        }
    }

    pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                when (event.type) {
                    PointerEventType.Press -> isPointerPressed = true
                    PointerEventType.Release -> isPointerPressed = false

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Tooltip(text: String, content: @Composable () -> Unit) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(5.dp),
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
